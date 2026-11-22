package com.trampix.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trampix.entidades.enums.StatusContratacao;
import com.trampix.entidades.enums.StatusServico;
import com.trampix.repositorios.ContratacaoRepositorio;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.ServicoRepositorio;
import com.trampix.repositorios.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock UsuarioRepositorio usuarios;
    @Mock PrestadorPerfilRepositorio prestadores;
    @Mock ServicoRepositorio servicos;
    @Mock ContratacaoRepositorio contratacoes;
    @InjectMocks DashboardService service;

    @Test
    void deveCalcularIndicadoresComContagensDoBanco() {
        when(usuarios.count()).thenReturn(18L);
        when(prestadores.countByAtivoTrue()).thenReturn(6L);
        when(servicos.countByStatus(StatusServico.ATIVO)).thenReturn(12L);
        when(contratacoes.countByStatus(StatusContratacao.PENDENTE)).thenReturn(3L);
        when(contratacoes.countByStatus(StatusContratacao.CONCLUIDA)).thenReturn(9L);

        DashboardService.Indicadores resultado = service.consultar();

        assertEquals(18L, resultado.totalUsuarios());
        assertEquals(6L, resultado.totalPrestadores());
        assertEquals(12L, resultado.servicosAtivos());
        assertEquals(3L, resultado.pedidosPendentes());
        assertEquals(9L, resultado.contratacoesConcluidas());
    }
}
