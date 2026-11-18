package com.trampix.recursos;

import java.time.LocalTime;
import java.util.List;
import java.util.Comparator;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.trampix.entidades.Disponibilidade;
import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.DiaSemana;
import com.trampix.seguranca.Permissoes;
import com.trampix.services.DisponibilidadeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/usuarios/{usuarioId}/disponibilidades")
public class DisponibilidadeRecurso {
 public record SubstituirRequisicao(@NotNull List<@NotNull @Valid Item> disponibilidades) {}
 public record Item(@NotNull DiaSemana diaSemana,@NotNull LocalTime horaInicio,@NotNull LocalTime horaFim){@AssertTrue(message="horaFim deve ser maior que horaInicio") public boolean isHoraFimDepoisDoInicio(){return horaInicio==null||horaFim==null||horaFim.isAfter(horaInicio);}}
 public record DisponibilidadeResposta(Long id,DiaSemana diaSemana,LocalTime horaInicio,LocalTime horaFim){static DisponibilidadeResposta de(Disponibilidade d){return new DisponibilidadeResposta(d.getId(),d.getDiaSemana(),d.getHoraInicio(),d.getHoraFim());}}
 private final DisponibilidadeService service;
 public DisponibilidadeRecurso(DisponibilidadeService service){this.service=service;}
 @GetMapping public List<DisponibilidadeResposta> listar(@PathVariable Long usuarioId){return service.listar(usuarioId).stream().map(DisponibilidadeResposta::de).toList();}
 @PutMapping @ResponseStatus(HttpStatus.OK) public List<DisponibilidadeResposta> substituir(@PathVariable Long usuarioId,@Valid @RequestBody SubstituirRequisicao requisicao,@AuthenticationPrincipal Usuario logado){Permissoes.exigirProprioOuAdmin(logado,usuarioId);return service.substituir(usuarioId,requisicao.disponibilidades()).stream().map(DisponibilidadeResposta::de).toList();}
}
