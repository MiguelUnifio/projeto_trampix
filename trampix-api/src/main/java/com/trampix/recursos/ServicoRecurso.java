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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.trampix.entidades.Usuario;
import com.trampix.services.ServicoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/servicos")
public class ServicoRecurso {
    private final ServicoService service;
    public ServicoRecurso(ServicoService service) { this.service=service; }
    @GetMapping public List<ServicoResposta> listar(@RequestParam(required=false) Long categoriaId,@RequestParam(required=false) Long subcategoriaId) { return service.listar(categoriaId,subcategoriaId); }
    @GetMapping("/meus") public List<ServicoResposta> meus(@AuthenticationPrincipal Usuario logado) { return service.meus(logado); }
    @GetMapping("/{id}") public ServicoResposta buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ServicoResposta cadastrar(@Valid @RequestBody ServicoRequisicao dados,@AuthenticationPrincipal Usuario logado) { return service.cadastrar(dados,logado); }
    @PutMapping("/{id}") public ServicoResposta atualizar(@PathVariable Long id,@Valid @RequestBody ServicoRequisicao dados,@AuthenticationPrincipal Usuario logado) { return service.atualizar(id,dados,logado); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id,@AuthenticationPrincipal Usuario logado) { service.excluir(id,logado); }
}
