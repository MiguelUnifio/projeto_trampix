package com.trampix.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.trampix.entidades.PrestadorPerfil;
import com.trampix.entidades.Servico;
import com.trampix.entidades.Subcategoria;
import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.Perfil;
import com.trampix.entidades.enums.StatusServico;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.recursos.CriadorSubcategoria;
import com.trampix.recursos.ServicoRequisicao;
import com.trampix.recursos.ServicoResposta;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.ServicoRepositorio;

@Service
public class ServicoService {
    private final ServicoRepositorio servicos;
    private final PrestadorPerfilRepositorio prestadores;
    private final CriadorSubcategoria criadorSubcategoria;
    public ServicoService(ServicoRepositorio servicos, PrestadorPerfilRepositorio prestadores, CriadorSubcategoria criadorSubcategoria) {
        this.servicos=servicos; this.prestadores=prestadores; this.criadorSubcategoria=criadorSubcategoria;
    }
    @Transactional(readOnly=true)
    public List<ServicoResposta> listar(Long categoriaId, Long subcategoriaId) {
        return servicos.filtrar(StatusServico.ATIVO,categoriaId,subcategoriaId).stream().map(ServicoResposta::de).toList();
    }
    @Transactional(readOnly=true)
    public List<ServicoResposta> meus(Usuario logado) {
        PrestadorPerfil p=prestadorDo(logado);
        return servicos.findByPrestadorIdOrderById(p.getId()).stream().map(ServicoResposta::de).toList();
    }
    @Transactional(readOnly=true)
    public ServicoResposta buscar(Long id) {
        return servicos.findById(id).filter(s->s.getStatus()==StatusServico.ATIVO).map(ServicoResposta::de)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"serviço não encontrado"));
    }
    @Transactional
    public ServicoResposta cadastrar(ServicoRequisicao dados, Usuario logado) {
        PrestadorPerfil prestador=travar(prestadorDo(logado));
        Subcategoria sub=criadorSubcategoria.paraServico(dados.subcategoriaId(),dados.categoriaId(),dados.novaSubcategoria(),logado);
        exigirSubcategoriaLivre(prestador,sub,null);
        Servico s=new Servico(); s.setPrestador(prestador); dados.aplicar(s,sub);
        return ServicoResposta.de(servicos.save(s));
    }
    @Transactional
    public ServicoResposta atualizar(Long id, ServicoRequisicao dados, Usuario logado) {
        Servico s=buscarDoDonoOuAdmin(id,logado);
        if(s.getStatus()==StatusServico.INATIVO) throw new RegraNegocioException("serviço inativo não pode ser editado");
        PrestadorPerfil p=travar(s.getPrestador());
        Subcategoria sub=criadorSubcategoria.paraServico(dados.subcategoriaId(),dados.categoriaId(),dados.novaSubcategoria(),logado);
        if(s.getStatus()==StatusServico.ATIVO) exigirSubcategoriaLivre(p,sub,id);
        dados.aplicar(s,sub); return ServicoResposta.de(servicos.save(s));
    }
    @Transactional
    public void excluir(Long id, Usuario logado) {
        Servico s=buscarDoDonoOuAdmin(id,logado); s.setStatus(StatusServico.INATIVO); servicos.save(s);
    }
    private PrestadorPerfil prestadorDo(Usuario logado) {
        return prestadores.findByUsuarioId(logado.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,
                "só prestador pode fazer isso; cadastre o primeiro serviço em POST /usuarios/{id}/prestador"));
    }
    private PrestadorPerfil travar(PrestadorPerfil p) { return prestadores.travar(p.getId()).orElseThrow(); }
    private Servico buscarDoDonoOuAdmin(Long id, Usuario logado) {
        Servico s=servicos.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"serviço não encontrado"));
        boolean dono=s.getPrestador().getUsuario().getId().equals(logado.getId());
        if(!dono && logado.getPerfil()!=Perfil.ADMIN) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"só o dono do serviço ou um ADMIN pode fazer isso");
        return s;
    }
    private void exigirSubcategoriaLivre(PrestadorPerfil p, Subcategoria sub, Long ignorado) {
        if(servicos.existeNaSubcategoria(p.getId(),sub.getId(),StatusServico.ATIVO,ignorado))
            throw new RegraNegocioException("você já tem um serviço ativo na subcategoria " + sub.getNome() + "; edite o serviço existente");
    }
}
