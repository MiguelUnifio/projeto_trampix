package com.trampix.services;

import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.StatusUsuario;
import com.trampix.repositorios.UsuarioRepositorio;
import com.trampix.seguranca.TokenServico;

@Service
public class AuthService {
 public record ResultadoLogin(String token,Instant expiraEm,Usuario usuario){}
 private final UsuarioRepositorio usuarios;private final PasswordEncoder encoder;private final TokenServico tokens;
 public AuthService(UsuarioRepositorio u,PasswordEncoder e,TokenServico t){usuarios=u;encoder=e;tokens=t;}
 public ResultadoLogin login(String email,String senha){
  String normalizado=email.trim().toLowerCase(Locale.ROOT);
  Usuario usuario=usuarios.findByEmail(normalizado).filter(u->encoder.matches(senha,u.getSenhaHash()))
   .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"e-mail ou senha inválidos"));
  if(usuario.getStatus()!=StatusUsuario.ATIVO)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"login não permitido: conta com status "+usuario.getStatus().name());
  TokenServico.Token token=tokens.gerar(usuario);return new ResultadoLogin(token.valor(),token.expiraEm(),usuario);
 }
}
