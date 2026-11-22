package com.trampix.recursos;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.trampix.entidades.Usuario;
import com.trampix.services.SubcategoriaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/categorias/{categoriaId}/subcategorias")
public class SubcategoriaRecurso {
 public record SubcategoriaRequisicao(@NotBlank @Size(min=3,max=40) String nome,@Size(max=200) String descricao){}
 public record AtualizarRequisicao(@NotBlank @Size(min=3,max=40) String nome,@Size(max=200) String descricao,com.trampix.entidades.enums.StatusSubcategoria status){}
 private final SubcategoriaService service;
 public SubcategoriaRecurso(SubcategoriaService service){this.service=service;}
 @GetMapping public List<SubcategoriaResposta> listar(@PathVariable Long categoriaId){return service.listar(categoriaId);}
 @GetMapping("/{id}") public SubcategoriaResposta buscar(@PathVariable Long categoriaId,@PathVariable Long id){return service.buscar(categoriaId,id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public SubcategoriaResposta criar(@PathVariable Long categoriaId,@Valid @RequestBody SubcategoriaRequisicao req,@AuthenticationPrincipal Usuario logado){return service.criar(categoriaId,req,logado);}
 @PutMapping("/{id}") public SubcategoriaResposta atualizar(@PathVariable Long categoriaId,@PathVariable Long id,@Valid @RequestBody AtualizarRequisicao req){return service.atualizar(categoriaId,id,req);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void inativar(@PathVariable Long categoriaId,@PathVariable Long id){service.inativar(categoriaId,id);}
}
