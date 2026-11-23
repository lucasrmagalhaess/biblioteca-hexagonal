package br.com.senac.biblioteca.domain.exception;

/**
 * Exceção lançada quando uma regra de negócio do domínio é violada.
 * Pertence ao núcleo: não conhece console, arquivos nem frameworks.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
