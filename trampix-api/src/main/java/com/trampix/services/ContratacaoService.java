package com.trampix.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.trampix.entidades.Contratacao;
import com.trampix.entidades.ContratacaoHistorico;
import com.trampix.entidades.Disponibilidade;
import com.trampix.entidades.Servico;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.entidades.enums.StatusContratacao;
import com.trampix.repositorios.ContratacaoHistoricoRepositorio;
import com.trampix.repositorios.ContratacaoRepositorio;
import com.trampix.repositorios.DisponibilidadeRepositorio;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.ServicoRepositorio;

/** Camada de acesso aos dados usados pelo fluxo transacional de contratação. */
@Service
public class ContratacaoService {
 private final ContratacaoRepositorio contratacoes; private final ContratacaoHistoricoRepositorio historicos;
 private final ServicoRepositorio servicos; private final DisponibilidadeRepositorio disponibilidades; private final PrestadorPerfilRepositorio prestadores;
 public ContratacaoService(ContratacaoRepositorio c,ContratacaoHistoricoRepositorio h,ServicoRepositorio s,DisponibilidadeRepositorio d,PrestadorPerfilRepositorio p){contratacoes=c;historicos=h;servicos=s;disponibilidades=d;prestadores=p;}
 @Transactional(readOnly=true) public Optional<Contratacao> buscar(Long id){return contratacoes.findById(id);}
 @Transactional public Contratacao salvar(Contratacao c){return contratacoes.save(c);}
 @Transactional public Contratacao salvarEAtualizar(Contratacao c){return contratacoes.saveAndFlush(c);}
 @Transactional(readOnly=true) public List<Contratacao> filtrar(Long usuarioId,boolean cliente,boolean prestador,Long servicoId){return contratacoes.filtrar(usuarioId,cliente,prestador,servicoId);}
 @Transactional(readOnly=true) public List<Contratacao> conflitos(Long prestadorId,StatusContratacao status,LocalDate inicio,LocalDate fim,Long ignorado){return contratacoes.doPrestadorNasDatas(prestadorId,status,inicio,fim,ignorado);}
 @Transactional public void salvarHistorico(ContratacaoHistorico h){historicos.save(h);}
 @Transactional(readOnly=true) public List<ContratacaoHistorico> historico(Long id){return historicos.findByContratacaoIdOrderByCriadoEmAscIdAsc(id);}
 @Transactional(readOnly=true) public Optional<Servico> buscarServico(Long id){return servicos.findById(id);}
 @Transactional(readOnly=true) public List<Disponibilidade> disponibilidades(Long prestadorId){return disponibilidades.findByPrestadorId(prestadorId);}
 /** Regra central do domínio: não aceitar duas contratações para o mesmo prestador no mesmo intervalo. */
 @Transactional(readOnly=true) public void exigirSemConflito(Servico servico,LocalDate inicio,LocalDate fim,LocalTime horaInicio,LocalTime horaFim,Long idIgnorado){
  List<Contratacao> aceitas=contratacoes.doPrestadorNasDatas(servico.getPrestador().getId(),StatusContratacao.ACEITA,inicio,fim,idIgnorado);
  for(Contratacao outra:aceitas){boolean conflita=horaInicio==null||outra.getHoraInicio()==null||(horaInicio.isBefore(outra.getHoraFim())&&outra.getHoraInicio().isBefore(horaFim));if(conflita)throw new RegraNegocioException("o prestador já tem uma contratação aceita que conflita com estas datas (contratação "+outra.getId()+")");}
 }
 public void exigirStatus(Contratacao contratacao,String acao,Set<StatusContratacao> permitidos){if(!permitidos.contains(contratacao.getStatus()))throw new RegraNegocioException("não é possível "+acao+" uma contratação "+contratacao.getStatus()+": só é permitido a partir de "+String.join(" ou ",permitidos.stream().map(Enum::name).toList()));}
 @Transactional public void travarPrestador(Long id){prestadores.travar(id).orElseThrow();}
}
