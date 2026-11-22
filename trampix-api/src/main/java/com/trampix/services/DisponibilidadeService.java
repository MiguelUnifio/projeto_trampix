package com.trampix.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.trampix.entidades.Disponibilidade;
import com.trampix.entidades.PrestadorPerfil;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.recursos.DisponibilidadeRecurso.Item;
import com.trampix.repositorios.DisponibilidadeRepositorio;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.UsuarioRepositorio;

@Service
public class DisponibilidadeService {

    private static final Comparator<Disponibilidade> ORDEM = Comparator.comparing(Disponibilidade::getDiaSemana)
            .thenComparing(Disponibilidade::getHoraInicio);

    private final UsuarioRepositorio usuarios;
    private final PrestadorPerfilRepositorio prestadores;
    private final DisponibilidadeRepositorio disponibilidades;

    public DisponibilidadeService(UsuarioRepositorio usuarios, PrestadorPerfilRepositorio prestadores,
            DisponibilidadeRepositorio disponibilidades) {
        this.usuarios = usuarios;
        this.prestadores = prestadores;
        this.disponibilidades = disponibilidades;
    }

    @Transactional(readOnly = true)
    public List<Disponibilidade> listar(Long usuarioId) {
        PrestadorPerfil prestador = prestadorOu404(usuarioId);
        return disponibilidades.findByPrestadorId(prestador.getId()).stream().sorted(ORDEM).toList();
    }

    @Transactional(readOnly = true)
    public Disponibilidade buscar(Long usuarioId, Long id) {
        PrestadorPerfil prestador = prestadorOu404(usuarioId);
        return disponibilidades.findByIdAndPrestadorId(id, prestador.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "disponibilidade não encontrada"));
    }

    // regra: horários do mesmo dia não podem se sobrepor
    @Transactional
    public Disponibilidade criar(Long usuarioId, Item item) {
        PrestadorPerfil prestador = prestadorParaAlterar(usuarioId);
        Disponibilidade nova = montar(prestador, item);
        recusarSobreposicao(nova, disponibilidades.findByPrestadorId(prestador.getId()));
        return disponibilidades.save(nova);
    }

    @Transactional
    public Disponibilidade atualizar(Long usuarioId, Long id, Item item) {
        PrestadorPerfil prestador = prestadorParaAlterar(usuarioId);
        Disponibilidade existente = disponibilidades.findByIdAndPrestadorId(id, prestador.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "disponibilidade não encontrada"));
        List<Disponibilidade> outras = disponibilidades.findByPrestadorId(prestador.getId()).stream()
                .filter(d -> !d.getId().equals(id)).toList();
        existente.setDiaSemana(item.diaSemana());
        existente.setHoraInicio(item.horaInicio());
        existente.setHoraFim(item.horaFim());
        recusarSobreposicao(existente, outras);
        return disponibilidades.save(existente);
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        PrestadorPerfil prestador = prestadorOu404(usuarioId);
        Disponibilidade existente = disponibilidades.findByIdAndPrestadorId(id, prestador.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "disponibilidade não encontrada"));
        disponibilidades.delete(existente);
    }

    @Transactional
    public List<Disponibilidade> substituir(Long usuarioId, List<Item> itens) {
        PrestadorPerfil prestador = prestadorParaAlterar(usuarioId);
        List<Disponibilidade> novas = itens.stream().map(item -> montar(prestador, item)).sorted(ORDEM).toList();
        recusarSobreposicaoEntreSi(novas);
        disponibilidades.apagarDoPrestador(prestador.getId());
        return disponibilidades.saveAll(novas);
    }

    private PrestadorPerfil prestadorOu404(Long usuarioId) {
        if (!usuarios.existsById(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário não encontrado");
        }
        return prestadores.findByUsuarioIdAndAtivoTrue(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "este usuário não é prestador"));
    }

    private PrestadorPerfil prestadorParaAlterar(Long usuarioId) {
        if (!usuarios.existsById(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário não encontrado");
        }
        return prestadores.findByUsuarioIdAndAtivoTrue(usuarioId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT, "só prestador tem disponibilidade: cadastre um serviço primeiro"));
    }

    private static Disponibilidade montar(PrestadorPerfil prestador, Item item) {
        Disponibilidade d = new Disponibilidade();
        d.setPrestador(prestador);
        d.setDiaSemana(item.diaSemana());
        d.setHoraInicio(item.horaInicio());
        d.setHoraFim(item.horaFim());
        return d;
    }

    private static void recusarSobreposicao(Disponibilidade nova, List<Disponibilidade> existentes) {
        for (Disponibilidade outra : existentes) {
            if (outra.getDiaSemana() == nova.getDiaSemana() && nova.getHoraInicio().isBefore(outra.getHoraFim())
                    && outra.getHoraInicio().isBefore(nova.getHoraFim())) {
                throw new RegraNegocioException(String.format("horários sobrepostos em %s: %s-%s e %s-%s",
                        nova.getDiaSemana(), outra.getHoraInicio(), outra.getHoraFim(), nova.getHoraInicio(),
                        nova.getHoraFim()));
            }
        }
    }

    private static void recusarSobreposicaoEntreSi(List<Disponibilidade> ordenadas) {
        for (int i = 1; i < ordenadas.size(); i++) {
            Disponibilidade a = ordenadas.get(i - 1);
            Disponibilidade b = ordenadas.get(i);
            if (a.getDiaSemana() == b.getDiaSemana() && b.getHoraInicio().isBefore(a.getHoraFim())) {
                throw new RegraNegocioException(String.format("horários sobrepostos em %s: %s-%s e %s-%s",
                        b.getDiaSemana(), a.getHoraInicio(), a.getHoraFim(), b.getHoraInicio(), b.getHoraFim()));
            }
        }
    }
}
