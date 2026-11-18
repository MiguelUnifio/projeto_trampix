package com.trampix.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.Perfil;
import com.trampix.entidades.enums.StatusUsuario;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.repositorios.UsuarioRepositorio;

@Service
public class UsuarioService {
    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepositorio repositorio, PasswordEncoder passwordEncoder) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() { return repositorio.findAll(); }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário não encontrado"));
    }

    @Transactional
    public Usuario cadastrar(Usuario usuario) {
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "senha é obrigatória");
        }
        if (repositorio.existsByEmail(usuario.getEmail()))
            throw new RegraNegocioException("e-mail já cadastrado");
        if (repositorio.existsByDocumento(usuario.getDocumento()))
            throw new RegraNegocioException("documento já cadastrado");
        usuario.setId(null);
        usuario.setPerfil(Perfil.USUARIO);
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.setSenhaHash(passwordEncoder.encode(usuario.getSenha()));
        return repositorio.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario dados) {
        Usuario usuario = buscar(id);
        if (repositorio.existsByEmailAndIdNot(dados.getEmail(), id))
            throw new RegraNegocioException("e-mail já cadastrado");
        usuario.setNome(dados.getNome()); usuario.setEmail(dados.getEmail());
        usuario.setDataNascimento(dados.getDataNascimento()); usuario.setTelefone(dados.getTelefone());
        usuario.setCep(dados.getCep()); usuario.setLogradouro(dados.getLogradouro());
        usuario.setNumero(dados.getNumero()); usuario.setComplemento(dados.getComplemento());
        usuario.setBairro(dados.getBairro()); usuario.setCidade(dados.getCidade()); usuario.setUf(dados.getUf());
        if (dados.getSenha() != null && !dados.getSenha().isBlank())
            usuario.setSenhaHash(passwordEncoder.encode(dados.getSenha()));
        return repositorio.save(usuario);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscar(id);
        usuario.setStatus(StatusUsuario.INATIVO);
        repositorio.save(usuario);
    }
}
