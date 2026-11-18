package com.trampix.recursos;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trampix.services.DashboardService;
import com.trampix.services.DashboardService.Indicadores;

@RestController
@RequestMapping("/dashboard")
public class DashboardRecurso {
    private final DashboardService service;
    public DashboardRecurso(DashboardService service) { this.service = service; }

    @GetMapping
    public Indicadores consultar() { return service.consultar(); }
}
