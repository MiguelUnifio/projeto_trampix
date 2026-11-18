package com.trampix.excecoes;

/** Exceção lançada quando uma operação viola uma regra do domínio Trampix. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
