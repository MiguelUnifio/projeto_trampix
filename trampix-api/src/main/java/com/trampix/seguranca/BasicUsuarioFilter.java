package com.trampix.seguranca;

import java.io.IOException;
import java.util.Locale;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.StatusUsuario;
import com.trampix.repositorios.UsuarioRepositorio;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Depois do HTTP Basic, troca o principal (um UserDetails) pelo {@link Usuario} do banco.
 * Assim os controllers, que usam {@code @AuthenticationPrincipal Usuario}, funcionam
 * igual com Basic e com JWT. Não é um @Component de propósito: é registrado só na cadeia de segurança.
 */
public class BasicUsuarioFilter extends OncePerRequestFilter {

    private final UsuarioRepositorio usuarios;

    public BasicUsuarioFilter(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication atual = SecurityContextHolder.getContext().getAuthentication();
        if (atual instanceof UsernamePasswordAuthenticationToken && atual.isAuthenticated()
                && !(atual.getPrincipal() instanceof Usuario)) {
            usuarios.findByEmail(atual.getName().trim().toLowerCase(Locale.ROOT))
                    .filter(u -> u.getStatus() == StatusUsuario.ATIVO)
                    .ifPresent(u -> {
                        UsernamePasswordAuthenticationToken novo = UsernamePasswordAuthenticationToken
                                .authenticated(u, null, atual.getAuthorities());
                        novo.setDetails(atual.getDetails());
                        SecurityContextHolder.getContext().setAuthentication(novo);
                    });
        }
        chain.doFilter(request, response);
    }
}
