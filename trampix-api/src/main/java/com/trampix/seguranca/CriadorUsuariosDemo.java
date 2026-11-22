package com.trampix.seguranca;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.Perfil;
import com.trampix.entidades.enums.StatusUsuario;
import com.trampix.entidades.enums.TipoDocumento;
import com.trampix.repositorios.UsuarioRepositorio;

/**
 * Garante no banco os dois usuários de demonstração do HTTP Basic (ADMIN e USUARIO), com o mesmo
 * e-mail e senha configurados em memória. Eles precisam existir no banco porque os endpoints
 * usam o usuário logado (id, perfil) para checar permissões.
 */
@Component
public class CriadorUsuariosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CriadorUsuariosDemo.class);
    // CPF de teste válido (apenas para o usuário de demonstração)
    private static final String CPF_DEMO = "52998224725";

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminSenha;
    private final String usuarioEmail;
    private final String usuarioSenha;

    public CriadorUsuariosDemo(UsuarioRepositorio repositorio, PasswordEncoder encoder,
            @Value("${trampix.basic.admin.usuario:admin@trampix.local}") String adminEmail,
            @Value("${trampix.basic.admin.senha:AdminTrampix123!}") String adminSenha,
            @Value("${trampix.basic.usuario.usuario:usuario@trampix.local}") String usuarioEmail,
            @Value("${trampix.basic.usuario.senha:UsuarioTrampix123!}") String usuarioSenha) {
        this.repositorio = repositorio;
        this.encoder = encoder;
        this.adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        this.adminSenha = adminSenha;
        this.usuarioEmail = usuarioEmail.trim().toLowerCase(Locale.ROOT);
        this.usuarioSenha = usuarioSenha;
    }

    @Override
    public void run(ApplicationArguments args) {
        criar("Administrador Demo", adminEmail, adminSenha, Perfil.ADMIN, null);
        criar("Usuário Demo", usuarioEmail, usuarioSenha, Perfil.USUARIO, CPF_DEMO);
    }

    private void criar(String nome, String email, String senha, Perfil perfil, String cpf) {
        if (repositorio.existsByEmail(email)) {
            return;
        }
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenhaHash(encoder.encode(senha));
        u.setPerfil(perfil);
        u.setStatus(StatusUsuario.ATIVO);
        if (cpf != null && !repositorio.existsByDocumento(cpf)) {
            u.setDocumento(cpf);
            u.setTipoDocumento(TipoDocumento.CPF);
        }
        try {
            repositorio.save(u);
            log.info("Usuário de demonstração {} ({}) criado no banco.", email, perfil);
        } catch (RuntimeException e) {
            log.warn("Não foi possível criar o usuário de demonstração {}: {}", email, e.getMessage());
        }
    }
}
