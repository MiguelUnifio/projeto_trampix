package com.trampix.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.trampix.entidades.Categoria;
import com.trampix.entidades.enums.StatusSubcategoria;
import com.trampix.excecoes.RegraNegocioException;
import com.trampix.recursos.CategoriaResposta;
import com.trampix.recursos.SubcategoriaResposta;
import com.trampix.repositorios.CategoriaRepositorio;
import com.trampix.repositorios.SubcategoriaRepositorio;

@Service
public class CategoriaService {
    private final CategoriaRepositorio categorias;
    private final SubcategoriaRepositorio subcategorias;
    public CategoriaService(CategoriaRepositorio categorias, SubcategoriaRepositorio subcategorias) {
        this.categorias = categorias; this.subcategorias = subcategorias;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResposta> listar() {
        Map<Long, List<SubcategoriaResposta>> porCategoria = subcategorias
                .findByStatusOrderById(StatusSubcategoria.ATIVA).stream().map(SubcategoriaResposta::de)
                .collect(Collectors.groupingBy(SubcategoriaResposta::categoriaId));
        return categorias.findByAtivaTrueOrderById().stream()
                .map(c -> CategoriaResposta.de(c, porCategoria.getOrDefault(c.getId(), List.of()))).toList();
    }
    @Transactional(readOnly = true)
    public CategoriaResposta buscar(Long id) { return resposta(buscarEntidade(id)); }
    @Transactional
    public CategoriaResposta cadastrar(Categoria categoria) {
        if (categorias.existsByNomeIgnoreCase(categoria.getNome()))
            throw new RegraNegocioException("já existe uma categoria com esse nome");
        categoria.setId(null); categoria.setAtiva(true);
        return resposta(categorias.save(categoria));
    }
    @Transactional
    public CategoriaResposta atualizar(Long id, Categoria dados) {
        Categoria categoria = buscarEntidade(id);
        if (categorias.existsByNomeIgnoreCaseAndIdNot(dados.getNome(), id))
            throw new RegraNegocioException("já existe uma categoria com esse nome");
        categoria.setNome(dados.getNome()); categoria.setDescricao(dados.getDescricao());
        return resposta(categorias.save(categoria));
    }
    @Transactional
    public void excluir(Long id) { Categoria categoria = buscarEntidade(id); categoria.setAtiva(false); categorias.save(categoria); }
    private Categoria buscarEntidade(Long id) {
        return categorias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "categoria não encontrada"));
    }
    private CategoriaResposta resposta(Categoria categoria) {
        return CategoriaResposta.de(categoria, subcategorias.findByCategoriaIdAndStatusOrderById(categoria.getId(), StatusSubcategoria.ATIVA)
                .stream().map(SubcategoriaResposta::de).toList());
    }
}
