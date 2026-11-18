package com.trampix.recursos;

import java.time.Instant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.trampix.entidades.Usuario;
import com.trampix.services.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/auth")
public class AuthRecurso {
 public record LoginRequisicao(@NotBlank @Email String email,@NotBlank String senha){}
 public record LoginResposta(String token,String tipo,Instant expiraEm,Usuario usuario){}
 private final AuthService service;
 public AuthRecurso(AuthService service){this.service=service;}
 @PostMapping("/login") public LoginResposta login(@Valid @RequestBody LoginRequisicao req){AuthService.ResultadoLogin r=service.login(req.email(),req.senha());return new LoginResposta(r.token(),"Bearer",r.expiraEm(),r.usuario());}
}
