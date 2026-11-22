package com.trampix.services;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.trampix.entidades.PrestadorPerfil;
import com.trampix.entidades.Servico;
import com.trampix.entidades.Subcategoria;
import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.StatusContratacao;
import com.trampix.entidades.enums.StatusServico;
import com.trampix.entidades.enums.StatusUsuario;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.recursos.PrestadorRecurso.AtualizarPrestadorRequisicao;
import com.trampix.recursos.PrestadorRecurso.PrestadorResposta;
import com.trampix.recursos.PrestadorRecurso.TornarPrestadorRequisicao;
import com.trampix.recursos.ServicoRequisicao;
import com.trampix.recursos.ServicoResposta;
import com.trampix.repositorios.ContratacaoRepositorio;
import com.trampix.repositorios.DisponibilidadeRepositorio;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.ServicoRepositorio;
import com.trampix.repositorios.UsuarioRepositorio;

@Service
public class PrestadorService {

    // contratações que ainda não terminaram
    static final Set<StatusContratacao> EM_ANDAMENTO = EnumSet.of(StatusContratacao.PENDENTE,
            StatusContratacao.CONTRAPROPOSTA, StatusContratacao.ACEITA);

    private final UsuarioRepositorio usuarios;
    private final PrestadorPerfilRepositorio prestadores;
    private final ServicoRepositorio servicos;
    private final ContratacaoRepositorio contratacoes;
    private final DisponibilidadeRepositorio disponibilidades;
    private final CriadorSubcategoria criador;

    public PrestadorService(UsuarioRepositorio usuarios, PrestadorPerfilRepositorio prestadores,
            ServicoRepositorio servicos, ContratacaoRepositorio contratacoes,
            DisponibilidadeRepositorio disponibilidades, CriadorSubcategoria criador) {
        this.usuarios = usuarios;
        this.prestadores = prestadores;
        this.servicos = servicos;
        this.contratacoes = contratacoes;
        this.disponibilidades = disponibilidades;
        this.criador = criador;
    }

    @Transactional(readOnly = true)
    public PrestadorResposta buscar(Long usuarioId) {
        return PrestadorResposta.de(perfilAtivo(usuarioId));
    }

    // cria o perfil (ou reativa um perfil encerrado) junto com o primeiro serviço
    @Transactional
    public ServicoResposta tornarPrestador(Long usuarioId, TornarPrestadorRequisicao req, Usuario logado) {
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário não encontrado"));
        if (usuario.getStatus() != StatusUsuario.ATIVO) {
            throw new RegraNegocioException("só usuário ATIVO pode virar prestador");
        }
        PrestadorPerfil perfil = prestadores.findByUsuarioId(usuarioId).orElse(null);
        if (perfil != null && perfil.isAtivo()) {
            throw new RegraNegocioException("este usuário já é prestador");
        }
        ServicoRequisicao dados = req.servico();
        Subcategoria sub = criador.paraServico(dados.subcategoriaId(), dados.categoriaId(),
                dados.novaSubcategoria(), logado);
        if (perfil == null) {
            perfil = new PrestadorPerfil();
            perfil.setUsuario(usuario);
        }
        perfil.setAtivo(true);
        perfil.setBiografia(req.biografia());
        perfil.setRaioAtendimentoKm(req.raioAtendimentoKm());
        prestadores.save(perfil);

        Servico servico = new Servico();
        servico.setPrestador(perfil);
        dados.aplicar(servico, sub);
        return ServicoResposta.de(servicos.save(servico));
    }

    @Transactional
    public PrestadorResposta atualizar(Long usuarioId, AtualizarPrestadorRequisicao req) {
        PrestadorPerfil perfil = perfilAtivo(usuarioId);
        perfil.setBiografia(req.biografia());
        perfil.setRaioAtendimentoKm(req.raioAtendimentoKm());
        perfil.setAgendaCheia(req.agendaCheia());
        return PrestadorResposta.de(prestadores.save(perfil));
    }

    // regra: não encerra o perfil enquanto houver contratação em andamento
    @Transactional
    public void encerrar(Long usuarioId) {
        PrestadorPerfil perfil = perfilAtivo(usuarioId);
        if (contratacoes.existsByServicoPrestadorIdAndStatusIn(perfil.getId(), EM_ANDAMENTO)) {
            throw new RegraNegocioException(
                    "o prestador tem contratações em andamento (PENDENTE, CONTRAPROPOSTA ou ACEITA): "
                            + "conclua ou cancele antes de encerrar o perfil");
        }
        servicos.findByPrestadorIdOrderById(perfil.getId()).stream()
                .filter(s -> s.getStatus() == StatusServico.ATIVO)
                .forEach(s -> {
                    s.setStatus(StatusServico.INATIVO);
                    servicos.save(s);
                });
        disponibilidades.apagarDoPrestador(perfil.getId());
        perfil.setAtivo(false);
        prestadores.save(perfil);
    }

    private PrestadorPerfil perfilAtivo(Long usuarioId) {
        if (!usuarios.existsById(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário não encontrado");
        }
        return prestadores.findByUsuarioIdAndAtivoTrue(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "este usuário não é prestador"));
    }
}
