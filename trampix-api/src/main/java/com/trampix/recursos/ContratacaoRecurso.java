package com.trampix.recursos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.trampix.entidades.ContratacaoHistorico;
import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.StatusContratacao;
import com.trampix.services.ContratacaoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// endpoints de contratação: só recebem a requisição e delegam ao ContratacaoService
@RestController
@RequestMapping("/contratacoes")
public class ContratacaoRecurso {

    public record CriarRequisicao(
            @NotNull Long servicoId,
            @NotNull LocalDate dataInicio,
            LocalDate dataFim,
            LocalTime horaInicio,
            @Min(1) @Max(24) Integer quantidadeHoras,
            @Size(max = 1000) String observacao) {
    }

    // edição do pedido pelo cliente (enquanto PENDENTE)
    public record AtualizarRequisicao(
            @NotNull LocalDate dataInicio,
            LocalDate dataFim,
            LocalTime horaInicio,
            @Min(1) @Max(24) Integer quantidadeHoras,
            @Size(max = 1000) String observacao) {
    }

    public record ContrapropostaRequisicao(
            @NotNull LocalDate dataInicio,
            LocalDate dataFim,
            LocalTime horaInicio,
            @Min(1) @Max(24) Integer quantidadeHoras,
            @DecimalMin(value = "0.01", message = "o preço deve ser maior que zero")
            @Digits(integer = 8, fraction = 2) BigDecimal precoCombinado,
            @NotBlank @Size(min = 10, max = 500, message = "o motivo deve ter entre 10 e 500 caracteres") String motivo) {
    }

    public record RecusaRequisicao(@Size(max = 500) String motivo) {
    }

    // resposta do histórico
    public record HistoricoResposta(Long id, StatusContratacao statusAnterior, StatusContratacao statusNovo,
            AutorResumo autor, LocalDate dataInicio, LocalDate dataFim, LocalTime horaInicio, LocalTime horaFim,
            BigDecimal precoCombinado, Integer diasContados, String motivo, LocalDateTime criadoEm) {

        public record AutorResumo(Long id, String nome) {
        }

        public static HistoricoResposta de(ContratacaoHistorico h) {
            AutorResumo autor = h.getAutor() == null ? null
                    : new AutorResumo(h.getAutor().getId(), h.getAutor().getNome());
            return new HistoricoResposta(h.getId(), h.getStatusAnterior(), h.getStatusNovo(), autor,
                    h.getDataInicio(), h.getDataFim(), h.getHoraInicio(), h.getHoraFim(), h.getPrecoCombinado(),
                    h.getDiasContados(), h.getMotivo(), h.getCriadoEm());
        }
    }

    private final ContratacaoService service;

    public ContratacaoRecurso(ContratacaoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratacaoResposta criar(@Valid @RequestBody CriarRequisicao requisicao,
            @AuthenticationPrincipal Usuario logado) {
        return service.criar(requisicao, logado);
    }

    // lista as contratações do usuário logado (ADMIN pode filtrar)
    @GetMapping
    public List<ContratacaoResposta> listar(@RequestParam(required = false) String papel,
            @RequestParam(required = false) Long usuarioId, @RequestParam(required = false) Long servicoId,
            @AuthenticationPrincipal Usuario logado) {
        return service.listar(papel, usuarioId, servicoId, logado);
    }

    @GetMapping("/{id}")
    public ContratacaoResposta buscar(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return service.buscar(id, logado);
    }

    @PutMapping("/{id}")
    public ContratacaoResposta atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarRequisicao requisicao,
            @AuthenticationPrincipal Usuario logado) {
        return service.atualizar(id, requisicao, logado);
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoResposta> historico(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return service.historico(id, logado);
    }

    @GetMapping("/{id}/historico/{historicoId}")
    public HistoricoResposta historicoItem(@PathVariable Long id, @PathVariable Long historicoId,
            @AuthenticationPrincipal Usuario logado) {
        return service.historicoItem(id, historicoId, logado);
    }

    @PostMapping("/{id}/aceitar")
    public ContratacaoResposta aceitar(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return service.aceitar(id, logado);
    }

    @PostMapping("/{id}/contraproposta")
    public ContratacaoResposta contrapropor(@PathVariable Long id,
            @Valid @RequestBody ContrapropostaRequisicao requisicao, @AuthenticationPrincipal Usuario logado) {
        return service.contrapropor(id, requisicao, logado);
    }

    @PostMapping("/{id}/aceitar-contraproposta")
    public ContratacaoResposta aceitarContraproposta(@PathVariable Long id,
            @AuthenticationPrincipal Usuario logado) {
        return service.aceitarContraproposta(id, logado);
    }

    // prestador recusa o pedido ou cliente recusa a contraproposta
    @PostMapping("/{id}/recusar")
    public ContratacaoResposta recusar(@PathVariable Long id,
            @Valid @RequestBody(required = false) RecusaRequisicao requisicao,
            @AuthenticationPrincipal Usuario logado) {
        return service.recusar(id, requisicao, logado);
    }

    @PostMapping("/{id}/cancelar")
    public ContratacaoResposta cancelar(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return service.cancelar(id, logado);
    }

    @PostMapping("/{id}/concluir")
    public ContratacaoResposta concluir(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return service.concluir(id, logado);
    }

    // exclusão lógica: cancela o pedido e preserva o histórico para auditoria
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        service.excluir(id, logado);
    }
}
