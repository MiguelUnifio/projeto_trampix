package com.trampix.services;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.trampix.entidades.Categoria;
import com.trampix.entidades.Subcategoria;
import com.trampix.entidades.Usuario;
import com.trampix.entidades.enums.Perfil;
import com.trampix.entidades.enums.StatusSubcategoria;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.recursos.CriadorSubcategoria;
import com.trampix.recursos.SubcategoriaRecurso.AtualizarRequisicao;
import com.trampix.recursos.SubcategoriaRecurso.SubcategoriaRequisicao;
import com.trampix.recursos.SubcategoriaResposta;
import com.trampix.repositorios.CategoriaRepositorio;
import com.trampix.repositorios.PrestadorPerfilRepositorio;
import com.trampix.repositorios.SubcategoriaRepositorio;

@Service
public class SubcategoriaService {
 private final SubcategoriaRepositorio subcategorias; private final CategoriaRepositorio categorias; private final PrestadorPerfilRepositorio prestadores; private final CriadorSubcategoria criador;
 public SubcategoriaService(SubcategoriaRepositorio s,CategoriaRepositorio c,PrestadorPerfilRepositorio p,CriadorSubcategoria cr){subcategorias=s;categorias=c;prestadores=p;criador=cr;}
 @Transactional(readOnly=true) public List<SubcategoriaResposta> listar(Long categoriaId){buscarCategoria(categoriaId);return subcategorias.findByCategoriaIdAndStatusOrderById(categoriaId,StatusSubcategoria.ATIVA).stream().map(SubcategoriaResposta::de).toList();}
 @Transactional public SubcategoriaResposta criar(Long categoriaId,SubcategoriaRequisicao req,Usuario logado){
  boolean admin=logado.getPerfil()==Perfil.ADMIN;if(!admin&&!prestadores.existsByUsuarioId(logado.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"só prestador ou ADMIN pode criar subcategoria");
  Categoria categoria=buscarCategoria(categoriaId);CriadorSubcategoria.Resultado resultado=criador.obterOuCriar(categoria,req.nome(),req.descricao(),logado);Subcategoria sub=resultado.subcategoria();
  if(sub.getStatus()!=StatusSubcategoria.ATIVA)throw new RegraNegocioException("já existe a subcategoria "+sub.getNome()+" nesta categoria, mas está inativa");return SubcategoriaResposta.de(sub);
 }
 @Transactional public SubcategoriaResposta atualizar(Long categoriaId,Long id,AtualizarRequisicao req){
  Subcategoria sub=buscarSubcategoria(categoriaId,id);CriadorSubcategoria.exigirNomeValido(req.nome());
  if(subcategorias.existsByCategoriaIdAndNomeNormalizadoAndIdNot(categoriaId,Subcategoria.normalizar(req.nome()),id))throw new RegraNegocioException("já existe uma subcategoria com esse nome nesta categoria");
  sub.setNome(req.nome());sub.setDescricao(req.descricao());if(req.status()!=null)sub.setStatus(req.status());return SubcategoriaResposta.de(subcategorias.save(sub));
 }
 @Transactional public void inativar(Long categoriaId,Long id){Subcategoria sub=buscarSubcategoria(categoriaId,id);sub.setStatus(StatusSubcategoria.INATIVA);subcategorias.save(sub);}
 private Categoria buscarCategoria(Long id){return categorias.findById(id).filter(Categoria::isAtiva).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"categoria não encontrada"));}
 private Subcategoria buscarSubcategoria(Long categoriaId,Long id){return subcategorias.findByIdAndCategoriaId(id,categoriaId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"subcategoria não encontrada"));}
}
