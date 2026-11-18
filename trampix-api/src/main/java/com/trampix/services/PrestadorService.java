package com.trampix.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.trampix.entidades.PrestadorPerfil;
import com.trampix.entidades.Servico;
import com.trampix.entidades.Subcategoria;
import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.StatusUsuario;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.recursos.CriadorSubcategoria;
import com.trampix.recursos.PrestadorRecurso.TornarPrestadorRequisicao;
import com.trampix.recursos.ServicoResposta;
import com.trampix.recursos.ServicoRequisicao;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.ServicoRepositorio;
import com.trampix.repositorios.UsuarioRepositorio;

@Service
public class PrestadorService {
 private final UsuarioRepositorio usuarios; private final PrestadorPerfilRepositorio prestadores; private final ServicoRepositorio servicos; private final CriadorSubcategoria criador;
 public PrestadorService(UsuarioRepositorio u,PrestadorPerfilRepositorio p,ServicoRepositorio s,CriadorSubcategoria c){usuarios=u;prestadores=p;servicos=s;criador=c;}
 @Transactional(readOnly=true) public PrestadorPerfil buscar(Long usuarioId){return prestadores.findByUsuarioId(usuarioId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"este usuário não é prestador"));}
 @Transactional public ServicoResposta tornarPrestador(Long usuarioId,TornarPrestadorRequisicao req,Usuario logado){
  Usuario usuario=usuarios.findById(usuarioId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"usuário não encontrado"));
  if(usuario.getStatus()!=StatusUsuario.ATIVO)throw new RegraNegocioException("só usuário ATIVO pode virar prestador");
  if(prestadores.existsByUsuarioId(usuarioId))throw new RegraNegocioException("este usuário já é prestador");
  ServicoRequisicao dados=req.servico(); Subcategoria sub=criador.paraServico(dados.subcategoriaId(),dados.categoriaId(),dados.novaSubcategoria(),logado);
  PrestadorPerfil perfil=new PrestadorPerfil();perfil.setUsuario(usuario);perfil.setBiografia(req.biografia());perfil.setRaioAtendimentoKm(req.raioAtendimentoKm());prestadores.save(perfil);
  Servico servico=new Servico();servico.setPrestador(perfil);dados.aplicar(servico,sub);return ServicoResposta.de(servicos.save(servico));
 }
}
