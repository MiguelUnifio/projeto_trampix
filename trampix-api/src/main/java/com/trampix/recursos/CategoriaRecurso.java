package com.trampix.recursos;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.trampix.entidades.Categoria;
import com.trampix.services.CategoriaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/categorias")
public class CategoriaRecurso {
    private final CategoriaService service;
    public CategoriaRecurso(CategoriaService service) { this.service = service; }
    @GetMapping public List<CategoriaResposta> listar() { return service.listar(); }
    @GetMapping("/{id}") public CategoriaResposta buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResposta cadastrar(@Valid @RequestBody Categoria categoria) { return service.cadastrar(categoria); }
    @PutMapping("/{id}") public CategoriaResposta atualizar(@PathVariable Long id, @Valid @RequestBody Categoria dados) { return service.atualizar(id, dados); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) { service.excluir(id); }
}
