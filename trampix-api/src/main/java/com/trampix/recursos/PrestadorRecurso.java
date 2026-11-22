package com.trampix.recursos;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.trampix.entidades.PrestadorPerfil;
import com.trampix.entidades.Usuario;
import com.trampix.seguranca.Permissoes;
import com.trampix.services.PrestadorService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// perfil de prestador de um usuário: consultar, tornar-se prestador, editar e encerrar
@RestController
@RequestMapping("/usuarios/{usuarioId}/prestador")
public class PrestadorRecurso {

    public record TornarPrestadorRequisicao(@Size(max = 1000) String biografia, @Min(0) Integer raioAtendimentoKm,
            @NotNull @Valid ServicoRequisicao servico) {
    }

    public record AtualizarPrestadorRequisicao(@Size(max = 1000) String biografia, @Min(0) Integer raioAtendimentoKm,
            boolean agendaCheia) {
    }

    public record PrestadorResposta(Long id, Long usuarioId, String nome, String biografia, Integer raioAtendimentoKm,
            boolean agendaCheia, boolean verificado, LocalDateTime criadoEm) {

        public static PrestadorResposta de(PrestadorPerfil p) {
            return new PrestadorResposta(p.getId(), p.getUsuario().getId(), p.getUsuario().getNome(),
                    p.getBiografia(), p.getRaioAtendimentoKm(), p.isAgendaCheia(), p.isVerificado(),
                    p.getCriadoEm());
        }
    }

    private final PrestadorService service;

    public PrestadorRecurso(PrestadorService service) {
        this.service = service;
    }

    @GetMapping
    public PrestadorResposta buscar(@PathVariable Long usuarioId, @AuthenticationPrincipal Usuario logado) {
        Permissoes.exigirProprioOuAdmin(logado, usuarioId);
        return service.buscar(usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResposta tornarPrestador(@PathVariable Long usuarioId,
            @Valid @RequestBody TornarPrestadorRequisicao requisicao, @AuthenticationPrincipal Usuario logado) {
        Permissoes.exigirProprioOuAdmin(logado, usuarioId);
        return service.tornarPrestador(usuarioId, requisicao, logado);
    }

    @PutMapping
    public PrestadorResposta atualizar(@PathVariable Long usuarioId,
            @Valid @RequestBody AtualizarPrestadorRequisicao requisicao, @AuthenticationPrincipal Usuario logado) {
        Permissoes.exigirProprioOuAdmin(logado, usuarioId);
        return service.atualizar(usuarioId, requisicao);
    }

    // encerra o perfil de prestador (exclusão lógica)
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void encerrar(@PathVariable Long usuarioId, @AuthenticationPrincipal Usuario logado) {
        Permissoes.exigirProprioOuAdmin(logado, usuarioId);
        service.encerrar(usuarioId);
    }
}
