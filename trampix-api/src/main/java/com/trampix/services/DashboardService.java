package com.trampix.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trampix.entidades.enums.StatusContratacao;
import com.trampix.entidades.enums.StatusServico;
import com.trampix.repositorios.ContratacaoRepositorio;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.ServicoRepositorio;
import com.trampix.repositorios.UsuarioRepositorio;

@Service
public class DashboardService {
    public record Indicadores(long totalUsuarios, long totalPrestadores, long servicosAtivos,
            long pedidosPendentes, long contratacoesConcluidas) {}

    private final UsuarioRepositorio usuarios;
    private final PrestadorPerfilRepositorio prestadores;
    private final ServicoRepositorio servicos;
    private final ContratacaoRepositorio contratacoes;

    public DashboardService(UsuarioRepositorio usuarios, PrestadorPerfilRepositorio prestadores,
            ServicoRepositorio servicos, ContratacaoRepositorio contratacoes) {
        this.usuarios = usuarios;
        this.prestadores = prestadores;
        this.servicos = servicos;
        this.contratacoes = contratacoes;
    }

    @Transactional(readOnly = true)
    public Indicadores consultar() {
        return new Indicadores(usuarios.count(), prestadores.count(),
                servicos.countByStatus(StatusServico.ATIVO),
                contratacoes.countByStatus(StatusContratacao.PENDENTE),
                contratacoes.countByStatus(StatusContratacao.CONCLUIDA));
    }
}
