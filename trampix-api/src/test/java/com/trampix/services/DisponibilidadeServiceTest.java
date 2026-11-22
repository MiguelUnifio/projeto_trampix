package com.trampix.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trampix.entidades.Disponibilidade;
import com.trampix.entidades.PrestadorPerfil;
import com.trampix.entidades.enums.DiaSemana;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.recursos.DisponibilidadeRecurso.Item;
import com.trampix.repositorios.DisponibilidadeRepositorio;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTest {
    @Mock UsuarioRepositorio usuarios;
    @Mock PrestadorPerfilRepositorio prestadores;
    @Mock DisponibilidadeRepositorio disponibilidades;
    @InjectMocks DisponibilidadeService service;

    @Test
    void naoDeveCriarHorarioSobrepostoNoMesmoDia() {
        PrestadorPerfil prestador = new PrestadorPerfil();
        prestador.setId(5L);
        Disponibilidade existente = new Disponibilidade();
        existente.setPrestador(prestador);
        existente.setDiaSemana(DiaSemana.SEGUNDA);
        existente.setHoraInicio(LocalTime.of(8, 0));
        existente.setHoraFim(LocalTime.of(12, 0));

        when(usuarios.existsById(1L)).thenReturn(true);
        when(prestadores.findByUsuarioIdAndAtivoTrue(1L)).thenReturn(Optional.of(prestador));
        when(disponibilidades.findByPrestadorId(5L)).thenReturn(List.of(existente));

        Item sobreposto = new Item(DiaSemana.SEGUNDA, LocalTime.of(10, 0), LocalTime.of(14, 0));

        assertThrows(RegraNegocioException.class, () -> service.criar(1L, sobreposto));
        verify(disponibilidades, never()).save(any());
    }
}
