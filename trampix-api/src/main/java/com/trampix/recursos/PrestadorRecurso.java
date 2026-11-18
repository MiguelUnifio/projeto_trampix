package com.trampix.recursos;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import com.trampix.entidades.PrestadorPerfil;
import com.trampix.entidades.Usuario;
import com.trampix.seguranca.Permissoes;
import com.trampix.services.PrestadorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/usuarios/{usuarioId}/prestador")
public class PrestadorRecurso {
 public record TornarPrestadorRequisicao(@Size(max=1000) String biografia,@Min(0) Integer raioAtendimentoKm,@NotNull @Valid ServicoRequisicao servico){}
 private final PrestadorService service;
 public PrestadorRecurso(PrestadorService service){this.service=service;}
 @GetMapping @Transactional(readOnly=true) public PrestadorPerfil buscar(@PathVariable Long usuarioId,@AuthenticationPrincipal Usuario logado){Permissoes.exigirProprioOuAdmin(logado,usuarioId);return service.buscar(usuarioId);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional public ServicoResposta tornarPrestador(@PathVariable Long usuarioId,@Valid @RequestBody TornarPrestadorRequisicao requisicao,@AuthenticationPrincipal Usuario logado){Permissoes.exigirProprioOuAdmin(logado,usuarioId);return service.tornarPrestador(usuarioId,requisicao,logado);}
}
