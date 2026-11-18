package com.trampix.services;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.trampix.entidades.Disponibilidade;
import com.trampix.entidades.PrestadorPerfil;
import com.trampix.entidades.enums.DiaSemana;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.recursos.DisponibilidadeRecurso.Item;
import com.trampix.repositorios.DisponibilidadeRepositorio;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.UsuarioRepositorio;

@Service
public class DisponibilidadeService {
 private static final Comparator<Disponibilidade> ORDEM=Comparator.comparing(Disponibilidade::getDiaSemana).thenComparing(Disponibilidade::getHoraInicio);
 private final UsuarioRepositorio usuarios; private final PrestadorPerfilRepositorio prestadores; private final DisponibilidadeRepositorio disponibilidades;
 public DisponibilidadeService(UsuarioRepositorio u,PrestadorPerfilRepositorio p,DisponibilidadeRepositorio d){usuarios=u;prestadores=p;disponibilidades=d;}
 @Transactional(readOnly=true) public List<Disponibilidade> listar(Long usuarioId){
  if(!usuarios.existsById(usuarioId))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"usuário não encontrado");
  PrestadorPerfil p=prestadores.findByUsuarioId(usuarioId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"este usuário não é prestador"));
  return disponibilidades.findByPrestadorId(p.getId()).stream().sorted(ORDEM).toList();
 }
 @Transactional public List<Disponibilidade> substituir(Long usuarioId,List<Item> itens){
  if(!usuarios.existsById(usuarioId))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"usuário não encontrado");
  PrestadorPerfil p=prestadores.findByUsuarioId(usuarioId).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"só prestador tem disponibilidade: cadastre um serviço primeiro"));
  List<Disponibilidade> novas=itens.stream().map(item->{Disponibilidade d=new Disponibilidade();d.setPrestador(p);d.setDiaSemana(item.diaSemana());d.setHoraInicio(item.horaInicio());d.setHoraFim(item.horaFim());return d;}).sorted(ORDEM).toList();
  recusarSobreposicao(novas); disponibilidades.apagarDoPrestador(p.getId()); return disponibilidades.saveAll(novas);
 }
 private static void recusarSobreposicao(List<Disponibilidade> ordenadas){
  for(int i=1;i<ordenadas.size();i++){Disponibilidade a=ordenadas.get(i-1),b=ordenadas.get(i);if(a.getDiaSemana()==b.getDiaSemana()&&b.getHoraInicio().isBefore(a.getHoraFim()))throw new RegraNegocioException(String.format("horários sobrepostos em %s: %s-%s e %s-%s",b.getDiaSemana(),a.getHoraInicio(),a.getHoraFim(),b.getHoraInicio(),b.getHoraFim()));}
 }
}
