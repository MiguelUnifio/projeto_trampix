package com.trampix.services;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.trampix.entidades.Contratacao;
import com.trampix.entidades.ContratacaoHistorico;
import com.trampix.entidades.Disponibilidade;
import com.trampix.entidades.Servico;
import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.DiaSemana;
import com.trampix.entidades.enums.Perfil;
import com.trampix.entidades.enums.StatusContratacao;
import com.trampix.entidades.enums.StatusServico;
import com.trampix.entidades.enums.StatusUsuario;
import com.trampix.entidades.enums.TipoCobranca;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.recursos.ContratacaoRecurso.AtualizarRequisicao;
import com.trampix.recursos.ContratacaoRecurso.ContrapropostaRequisicao;
import com.trampix.recursos.ContratacaoRecurso.CriarRequisicao;
import com.trampix.recursos.ContratacaoRecurso.HistoricoResposta;
import com.trampix.recursos.ContratacaoRecurso.RecusaRequisicao;
import com.trampix.recursos.ContratacaoResposta;
import com.trampix.repositorios.ContratacaoHistoricoRepositorio;
import com.trampix.repositorios.ContratacaoRepositorio;
import com.trampix.repositorios.DisponibilidadeRepositorio;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.ServicoRepositorio;

/**
 * Regras de negócio da contratação de serviços: criação do pedido, validação de datas e
 * disponibilidade, conflito de agenda, cálculo de preço e transições de status
 * (PENDENTE → CONTRAPROPOSTA/ACEITA/RECUSADA/CANCELADA/EXPIRADA → CONCLUIDA).
 */
@Service
public class ContratacaoService {

    // fuso horário de Brasília
    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    // prazo para responder um pedido ou uma contraproposta
    public static final Duration PRAZO_RESPOSTA = Duration.ofDays(7);

    // datas e horas de um pedido (horas nulas quando é por dia)
    private record Periodo(LocalDate dataInicio, LocalDate dataFim, LocalTime horaInicio, LocalTime horaFim) {

        boolean porHora() {
            return horaInicio != null;
        }

        long horas() {
            return Duration.between(horaInicio, horaFim).toHours();
        }
    }

    private final ContratacaoRepositorio contratacoes;
    private final ContratacaoHistoricoRepositorio historicos;
    private final ServicoRepositorio servicos;
    private final DisponibilidadeRepositorio disponibilidades;
    private final PrestadorPerfilRepositorio prestadores;
    private final ExpiradorContratacao expirador;

    public ContratacaoService(ContratacaoRepositorio contratacoes, ContratacaoHistoricoRepositorio historicos,
            ServicoRepositorio servicos, DisponibilidadeRepositorio disponibilidades,
            PrestadorPerfilRepositorio prestadores, ExpiradorContratacao expirador) {
        this.contratacoes = contratacoes;
        this.historicos = historicos;
        this.servicos = servicos;
        this.disponibilidades = disponibilidades;
        this.prestadores = prestadores;
        this.expirador = expirador;
    }

    // ------------------------------------------------------------------ consultas

    @Transactional
    public List<ContratacaoResposta> listar(String papel, Long usuarioId, Long servicoId, Usuario logado) {
        boolean admin = logado.getPerfil() == Perfil.ADMIN;
        if (!admin && (usuarioId != null || servicoId != null)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "os filtros usuarioId e servicoId são só para ADMIN");
        }
        boolean filtroDeAdmin = admin && (usuarioId != null || servicoId != null);
        Long alvo = filtroDeAdmin ? usuarioId : logado.getId();
        boolean comoCliente = true;
        boolean comoPrestador = true;
        if (papel != null && !papel.isBlank()) {
            if (alvo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "o filtro papel precisa de usuarioId");
            }
            switch (papel.trim().toLowerCase(Locale.ROOT)) {
                case "cliente" -> comoPrestador = false;
                case "prestador" -> comoCliente = false;
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "papel inválido: use cliente ou prestador");
            }
        }
        expirador.expirarVencidas();
        return contratacoes.filtrar(alvo, comoCliente, comoPrestador, filtroDeAdmin ? servicoId : null).stream()
                .map(c -> ContratacaoResposta.de(c, logado)).toList();
    }

    @Transactional
    public ContratacaoResposta buscar(Long id, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        exigirParteOuAdmin(contratacao, logado);
        return ContratacaoResposta.de(contratacao, logado);
    }

    @Transactional
    public List<HistoricoResposta> historico(Long id, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        exigirParteOuAdmin(contratacao, logado);
        return historicos.findByContratacaoIdOrderByCriadoEmAscIdAsc(id).stream().map(HistoricoResposta::de).toList();
    }

    @Transactional
    public HistoricoResposta historicoItem(Long id, Long historicoId, Usuario logado) {
        Contratacao contratacao = buscarOu404(id);
        exigirParteOuAdmin(contratacao, logado);
        return historicos.findByIdAndContratacaoId(historicoId, id).map(HistoricoResposta::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "registro de histórico não encontrado"));
    }

    // ------------------------------------------------------------------ criação e edição

    @Transactional
    public ContratacaoResposta criar(CriarRequisicao requisicao, Usuario logado) {
        Servico servico = servicos.findById(requisicao.servicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "serviço não encontrado"));
        if (servico.getStatus() != StatusServico.ATIVO) {
            throw regra("este serviço não está disponível para contratação (status " + servico.getStatus() + ")");
        }
        if (logado.getStatus() != StatusUsuario.ATIVO) {
            throw regra("só usuário ATIVO pode contratar");
        }
        Usuario prestador = servico.getPrestador().getUsuario();
        if (prestador.getStatus() != StatusUsuario.ATIVO) {
            throw regra("o prestador deste serviço não está ativo");
        }
        if (!servico.getPrestador().isAtivo()) {
            throw regra("o prestador deste serviço encerrou o perfil profissional");
        }
        if (prestador.getId().equals(logado.getId())) {
            throw regra("você não pode contratar o seu próprio serviço");
        }
        Periodo periodo = montarPeriodo(servico, requisicao.dataInicio(), requisicao.dataFim(),
                requisicao.horaInicio(), requisicao.quantidadeHoras());
        exigirDataValida(periodo);
        int dias = diasAtendidos(servico, periodo);
        exigirSemConflito(servico, periodo, 0L);

        Contratacao contratacao = new Contratacao();
        contratacao.setServico(servico);
        contratacao.setCliente(logado);
        contratacao.setStatus(StatusContratacao.PENDENTE);
        aplicarPeriodo(contratacao, periodo);
        contratacao.setObservacao(requisicao.observacao());
        contratacao.setDiasContados(dias);
        contratacao.setPrecoCombinado(calcularPreco(servico, periodo, dias));
        contratacoes.save(contratacao);
        historicos.save(new ContratacaoHistorico(contratacao, logado, null, null));
        return ContratacaoResposta.de(contratacao, logado);
    }

    // o cliente edita o próprio pedido enquanto o prestador ainda não respondeu
    @Transactional
    public ContratacaoResposta atualizar(Long id, AtualizarRequisicao requisicao, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        if (!ehCliente(contratacao, logado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "só o cliente que fez o pedido pode editá-lo");
        }
        exigirStatus(contratacao, "editar", EnumSet.of(StatusContratacao.PENDENTE));
        Servico servico = contratacao.getServico();
        Periodo periodo = montarPeriodo(servico, requisicao.dataInicio(), requisicao.dataFim(),
                requisicao.horaInicio(), requisicao.quantidadeHoras());
        exigirDataValida(periodo);
        int dias = diasAtendidos(servico, periodo);
        exigirSemConflito(servico, periodo, contratacao.getId());

        aplicarPeriodo(contratacao, periodo);
        contratacao.setDiasContados(dias);
        contratacao.setObservacao(requisicao.observacao());
        contratacao.setPrecoCombinado(calcularPreco(servico, periodo, dias));
        return mudarStatus(contratacao, StatusContratacao.PENDENTE, logado, "pedido editado pelo cliente");
    }

    // ------------------------------------------------------------------ transições de status

    @Transactional
    public ContratacaoResposta aceitar(Long id, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        exigirPrestador(contratacao, logado, "aceitar");
        exigirStatus(contratacao, "aceitar", EnumSet.of(StatusContratacao.PENDENTE));
        travarPrestador(contratacao);
        Periodo periodo = periodoDe(contratacao);
        exigirDataValida(periodo);
        exigirSemConflito(contratacao.getServico(), periodo, contratacao.getId());
        return mudarStatus(contratacao, StatusContratacao.ACEITA, logado, null);
    }

    @Transactional
    public ContratacaoResposta contrapropor(Long id, ContrapropostaRequisicao requisicao, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        exigirPrestador(contratacao, logado, "fazer contraproposta");
        if (contratacao.getContrapropostaEm() != null) {
            throw regra("o prestador já fez uma contraproposta neste pedido");
        }
        exigirStatus(contratacao, "fazer contraproposta em", EnumSet.of(StatusContratacao.PENDENTE));
        Servico servico = contratacao.getServico();
        Periodo periodo = montarPeriodo(servico, requisicao.dataInicio(), requisicao.dataFim(),
                requisicao.horaInicio(), requisicao.quantidadeHoras());
        exigirDataValida(periodo);
        int dias = diasAtendidos(servico, periodo);
        exigirSemConflito(servico, periodo, contratacao.getId());

        aplicarPeriodo(contratacao, periodo);
        contratacao.setDiasContados(dias);
        contratacao.setPrecoCombinado(requisicao.precoCombinado() != null
                ? requisicao.precoCombinado() : calcularPreco(servico, periodo, dias));
        contratacao.setMotivoContraproposta(requisicao.motivo());
        contratacao.setContrapropostaEm(LocalDateTime.now());
        return mudarStatus(contratacao, StatusContratacao.CONTRAPROPOSTA, logado, requisicao.motivo());
    }

    @Transactional
    public ContratacaoResposta aceitarContraproposta(Long id, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        if (!ehCliente(contratacao, logado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "só o cliente pode aceitar a contraproposta");
        }
        exigirStatus(contratacao, "aceitar a contraproposta de", EnumSet.of(StatusContratacao.CONTRAPROPOSTA));
        travarPrestador(contratacao);
        Periodo periodo = periodoDe(contratacao);
        exigirDataValida(periodo);
        diasAtendidos(contratacao.getServico(), periodo);
        exigirSemConflito(contratacao.getServico(), periodo, contratacao.getId());
        return mudarStatus(contratacao, StatusContratacao.ACEITA, logado, null);
    }

    // prestador recusa o pedido ou cliente recusa a contraproposta
    @Transactional
    public ContratacaoResposta recusar(Long id, RecusaRequisicao requisicao, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        if (ehPrestador(contratacao, logado)) {
            exigirStatus(contratacao, "recusar (como prestador)", EnumSet.of(StatusContratacao.PENDENTE));
        } else if (ehCliente(contratacao, logado)) {
            exigirStatus(contratacao, "recusar (como cliente)", EnumSet.of(StatusContratacao.CONTRAPROPOSTA));
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "só o cliente ou o prestador podem recusar esta contratação");
        }
        String motivo = requisicao == null ? null : requisicao.motivo();
        contratacao.setMotivoRecusa(motivo);
        return mudarStatus(contratacao, StatusContratacao.RECUSADA, logado, motivo);
    }

    @Transactional
    public ContratacaoResposta cancelar(Long id, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        if (!ehCliente(contratacao, logado) && !ehPrestador(contratacao, logado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "só o cliente ou o prestador podem cancelar esta contratação");
        }
        exigirStatus(contratacao, "cancelar", EnumSet.of(StatusContratacao.PENDENTE,
                StatusContratacao.CONTRAPROPOSTA, StatusContratacao.ACEITA));
        return mudarStatus(contratacao, StatusContratacao.CANCELADA, logado, null);
    }

    // exclusão lógica: cancela o pedido e preserva o histórico para auditoria
    @Transactional
    public void excluir(Long id, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        if (!ehCliente(contratacao, logado) && !ehPrestador(contratacao, logado)
                && logado.getPerfil() != Perfil.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "só o cliente, o prestador ou um ADMIN podem excluir esta contratação");
        }
        exigirStatus(contratacao, "excluir/cancelar", EnumSet.of(StatusContratacao.PENDENTE,
                StatusContratacao.CONTRAPROPOSTA, StatusContratacao.ACEITA));
        mudarStatus(contratacao, StatusContratacao.CANCELADA, logado, "cancelada pela operação DELETE");
    }

    @Transactional
    public ContratacaoResposta concluir(Long id, Usuario logado) {
        expirador.expirarVencidas();
        Contratacao contratacao = buscarOu404(id);
        exigirPrestador(contratacao, logado, "concluir");
        exigirStatus(contratacao, "concluir", EnumSet.of(StatusContratacao.ACEITA));
        if (LocalDate.now(FUSO).isBefore(contratacao.getDataInicio())) {
            throw regra("a contratação só pode ser concluída a partir da data de início ("
                    + contratacao.getDataInicio() + ")");
        }
        return mudarStatus(contratacao, StatusContratacao.CONCLUIDA, logado, null);
    }

    // ------------------------------------------------------------------ regras de período, preço e agenda

    // monta as datas e horas conforme o tipo de cobrança do serviço
    private static Periodo montarPeriodo(Servico servico, LocalDate dataInicio, LocalDate dataFim,
            LocalTime horaInicio, Integer quantidadeHoras) {
        if (servico.getTipoCobranca() == TipoCobranca.POR_HORA) {
            if (horaInicio == null || quantidadeHoras == null) {
                throw invalido("serviço cobrado por hora: informe horaInicio e quantidadeHoras");
            }
            if (dataFim != null && !dataFim.equals(dataInicio)) {
                throw invalido("serviço cobrado por hora: o pedido é de um dia só (não informe outra dataFim)");
            }
            LocalDateTime inicio = dataInicio.atTime(horaInicio.truncatedTo(ChronoUnit.MINUTES));
            LocalDateTime fim = inicio.plusHours(quantidadeHoras);
            if (!fim.toLocalDate().equals(dataInicio)) {
                throw regra("o atendimento por hora precisa terminar no mesmo dia");
            }
            return new Periodo(dataInicio, dataInicio, inicio.toLocalTime(), fim.toLocalTime());
        }
        if (horaInicio != null || quantidadeHoras != null) {
            throw invalido("serviço cobrado por " + servico.getTipoCobranca()
                    + ": informe só dataInicio e dataFim, sem horaInicio e quantidadeHoras");
        }
        LocalDate fim = dataFim == null ? dataInicio : dataFim;
        if (fim.isBefore(dataInicio)) {
            throw regra("a dataFim não pode ser antes da dataInicio");
        }
        return new Periodo(dataInicio, fim, null, null);
    }

    private static Periodo periodoDe(Contratacao c) {
        return new Periodo(c.getDataInicio(), c.getDataFim(), c.getHoraInicio(), c.getHoraFim());
    }

    private static void aplicarPeriodo(Contratacao c, Periodo periodo) {
        c.setDataInicio(periodo.dataInicio());
        c.setDataFim(periodo.dataFim());
        c.setHoraInicio(periodo.horaInicio());
        c.setHoraFim(periodo.horaFim());
    }

    // calcula o preço pelo tipo de cobrança
    private static BigDecimal calcularPreco(Servico servico, Periodo periodo, int dias) {
        return switch (servico.getTipoCobranca()) {
            case POR_HORA -> servico.getPreco().multiply(BigDecimal.valueOf(periodo.horas()));
            case POR_DIA -> servico.getPreco().multiply(BigDecimal.valueOf(dias));
            case POR_TRABALHO, POR_M2 -> servico.getPreco();
        };
    }

    // confere se a data é hoje ou no futuro
    private static void exigirDataValida(Periodo periodo) {
        LocalDateTime agora = LocalDateTime.now(FUSO);
        if (periodo.dataInicio().isBefore(agora.toLocalDate())) {
            throw regra("a dataInicio precisa ser hoje ou no futuro (horário de Brasília)");
        }
        if (periodo.porHora() && !periodo.dataInicio().atTime(periodo.horaInicio()).isAfter(agora)) {
            throw regra("o horário de início já passou (horário de Brasília)");
        }
    }

    // confere a disponibilidade e devolve quantos dias do período o prestador atende
    private int diasAtendidos(Servico servico, Periodo periodo) {
        List<Disponibilidade> janelas = disponibilidades.findByPrestadorId(servico.getPrestador().getId());
        if (periodo.porHora()) {
            DiaSemana dia = DiaSemana.de(periodo.dataInicio().getDayOfWeek());
            boolean cabe = janelas.stream().anyMatch(d -> d.getDiaSemana() == dia
                    && !periodo.horaInicio().isBefore(d.getHoraInicio())
                    && !periodo.horaFim().isAfter(d.getHoraFim()));
            if (!cabe) {
                throw regra("o prestador não atende neste horário: " + periodo.horaInicio() + "-"
                        + periodo.horaFim() + " precisa caber numa disponibilidade de " + dia);
            }
            return 1;
        }
        Set<DiaSemana> diasComJanela = janelas.stream().map(Disponibilidade::getDiaSemana)
                .collect(Collectors.toSet());
        // dias sem disponibilidade não contam
        int dias = 0;
        for (LocalDate data = periodo.dataInicio(); !data.isAfter(periodo.dataFim()); data = data.plusDays(1)) {
            if (diasComJanela.contains(DiaSemana.de(data.getDayOfWeek()))) {
                dias++;
            }
        }
        if (dias == 0) {
            throw regra("o prestador não atende em nenhum dia do período de " + periodo.dataInicio() + " a "
                    + periodo.dataFim() + "; escolha datas em que ele tenha disponibilidade");
        }
        return dias;
    }

    /** Regra central do domínio: não aceitar duas contratações para o mesmo prestador no mesmo intervalo. */
    private void exigirSemConflito(Servico servico, Periodo periodo, Long idIgnorado) {
        List<Contratacao> aceitas = contratacoes.doPrestadorNasDatas(servico.getPrestador().getId(),
                StatusContratacao.ACEITA, periodo.dataInicio(), periodo.dataFim(), idIgnorado);
        for (Contratacao outra : aceitas) {
            boolean conflita = periodo.horaInicio() == null || outra.getHoraInicio() == null
                    || (periodo.horaInicio().isBefore(outra.getHoraFim())
                            && outra.getHoraInicio().isBefore(periodo.horaFim()));
            if (conflita) {
                throw regra("o prestador já tem uma contratação aceita que conflita com estas datas (contratação "
                        + outra.getId() + ")");
            }
        }
    }

    // ------------------------------------------------------------------ apoio

    private void travarPrestador(Contratacao contratacao) {
        prestadores.travar(contratacao.getServico().getPrestador().getId()).orElseThrow();
    }

    private static void exigirStatus(Contratacao contratacao, String acao, Set<StatusContratacao> permitidos) {
        if (!permitidos.contains(contratacao.getStatus())) {
            throw new RegraNegocioException("não é possível " + acao + " uma contratação " + contratacao.getStatus()
                    + ": só é permitido a partir de "
                    + String.join(" ou ", permitidos.stream().map(Enum::name).toList()));
        }
    }

    private static void exigirPrestador(Contratacao contratacao, Usuario logado, String acao) {
        if (!ehPrestador(contratacao, logado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "só o prestador do serviço pode " + acao);
        }
    }

    private static void exigirParteOuAdmin(Contratacao contratacao, Usuario logado) {
        boolean parte = ehCliente(contratacao, logado) || ehPrestador(contratacao, logado);
        if (!parte && logado.getPerfil() != Perfil.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "só o cliente, o prestador ou um ADMIN podem ver esta contratação");
        }
    }

    // muda o status e salva no histórico
    private ContratacaoResposta mudarStatus(Contratacao contratacao, StatusContratacao novo, Usuario logado,
            String motivo) {
        StatusContratacao anterior = contratacao.getStatus();
        contratacao.setStatus(novo);
        contratacoes.saveAndFlush(contratacao);
        historicos.save(new ContratacaoHistorico(contratacao, logado, anterior, motivo));
        return ContratacaoResposta.de(contratacao, logado);
    }

    private static boolean ehCliente(Contratacao contratacao, Usuario logado) {
        return contratacao.getCliente().getId().equals(logado.getId());
    }

    private static boolean ehPrestador(Contratacao contratacao, Usuario logado) {
        return contratacao.getServico().getPrestador().getUsuario().getId().equals(logado.getId());
    }

    private Contratacao buscarOu404(Long id) {
        return contratacoes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "contratação não encontrada"));
    }

    private static RegraNegocioException regra(String mensagem) {
        return new RegraNegocioException(mensagem);
    }

    private static ResponseStatusException invalido(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
