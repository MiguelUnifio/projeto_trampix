package com.trampix.seguranca;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.trampix.entidades.enums.StatusUsuario;
import com.trampix.repositorios.UsuarioRepositorio;

@Configuration
@EnableConfigurationProperties({ JwtPropriedades.class, CorsPropriedades.class })
public class SegurancaConfig {

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, ConversorJwtUsuario conversor, UsuarioRepositorio usuarios,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) throws Exception {
        // 401 e 403 no formato do TratadorErros
        AuthenticationEntryPoint naoAutenticado = (req, res, ex) -> resolver.resolveException(req, res, null, ex);
        AccessDeniedHandler semPermissao = (req, res, ex) -> resolver.resolveException(req, res, null, ex);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, "/usuarios", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/dashboard").authenticated()
                        .requestMatchers(HttpMethod.GET, "/categorias", "/categorias/**").permitAll()
                        // serviços do prestador logado
                        .requestMatchers(HttpMethod.GET, "/servicos/meus").authenticated()
                        .requestMatchers(HttpMethod.GET, "/servicos", "/servicos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios/*/disponibilidades", "/usuarios/*/disponibilidades/*").permitAll()
                        // prestador ou ADMIN cria subcategoria
                        .requestMatchers(HttpMethod.POST, "/categorias/*/subcategorias").authenticated()
                        // rotas só de ADMIN
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
                        .requestMatchers("/categorias", "/categorias/**").hasRole("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .addFilterAfter(new BasicUsuarioFilter(usuarios), BasicAuthenticationFilter.class)
                .oauth2ResourceServer(o -> o
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversor))
                        .authenticationEntryPoint(naoAutenticado))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(naoAutenticado)
                        .accessDeniedHandler(semPermissao));
        return http.build();
    }

    /** Usuários de demonstração exigidos no trabalho (HTTP Basic). Altere por variáveis de ambiente. */
    @Bean
    public UserDetailsService usuariosDeDemonstracao(PasswordEncoder encoder, UsuarioRepositorio usuarios,
            @Value("${trampix.basic.admin.usuario:admin@trampix.local}") String adminUsuario,
            @Value("${trampix.basic.admin.senha:AdminTrampix123!}") String adminSenha,
            @Value("${trampix.basic.usuario.usuario:usuario@trampix.local}") String usuario,
            @Value("${trampix.basic.usuario.senha:UsuarioTrampix123!}") String usuarioSenha) {
        UserDetails admin = User.withUsername(adminUsuario).password(encoder.encode(adminSenha)).roles("ADMIN").build();
        UserDetails comum = User.withUsername(usuario).password(encoder.encode(usuarioSenha)).roles("USUARIO").build();
        InMemoryUserDetailsManager memoria = new InMemoryUserDetailsManager(admin, comum);
        // usuários em memória primeiro; depois as contas do banco (e-mail + senha), também aceitas no Basic
        return nome -> {
            try {
                return memoria.loadUserByUsername(nome);
            } catch (UsernameNotFoundException e) {
                var conta = usuarios.findByEmail(nome.trim().toLowerCase(Locale.ROOT))
                        .orElseThrow(() -> new UsernameNotFoundException("usuário não encontrado"));
                return User.withUsername(conta.getEmail()).password(conta.getSenhaHash())
                        .roles(conta.getPerfil().name())
                        .disabled(conta.getStatus() != StatusUsuario.ATIVO).build();
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtPropriedades propriedades) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave(propriedades)));
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtPropriedades propriedades) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave(propriedades))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        // confere a validade e o emissor do token
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenServico.EMISSOR));
        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsPropriedades propriedades) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(propriedades.origens());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", cors);
        return fonte;
    }

    private static SecretKey chave(JwtPropriedades propriedades) {
        return new SecretKeySpec(propriedades.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
