package com.trampix.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trampix.entidades.Categoria;
import com.trampix.entidades.enums.StatusServico;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.repositorios.CategoriaRepositorio;
import com.trampix.repositorios.ServicoRepositorio;
import com.trampix.repositorios.SubcategoriaRepositorio;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {
    @Mock CategoriaRepositorio categorias;
    @Mock SubcategoriaRepositorio subcategorias;
    @Mock ServicoRepositorio servicos;
    @InjectMocks CategoriaService service;

    private Categoria categoria() {
        Categoria c = new Categoria();
        c.setId(1L);
        c.setNome("Pedreiro");
        return c;
    }

    @Test
    void naoDeveExcluirCategoriaComServicosAtivos() {
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria()));
        when(servicos.existsByCategoriaIdAndStatus(1L, StatusServico.ATIVO)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.excluir(1L));
        verify(categorias, never()).save(any());
    }

    @Test
    void deveInativarCategoriaSemServicosAtivos() {
        Categoria c = categoria();
        when(categorias.findById(1L)).thenReturn(Optional.of(c));
        when(servicos.existsByCategoriaIdAndStatus(1L, StatusServico.ATIVO)).thenReturn(false);

        service.excluir(1L);

        assertFalse(c.isAtiva());
        verify(categorias).save(c);
    }

    @Test
    void naoDeveCadastrarCategoriaComNomeDuplicado() {
        when(categorias.existsByNomeIgnoreCase("Pedreiro")).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.cadastrar(categoria()));
        verify(categorias, never()).save(any());
    }
}
