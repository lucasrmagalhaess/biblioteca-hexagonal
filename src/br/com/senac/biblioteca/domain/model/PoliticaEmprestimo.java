package br.com.senac.biblioteca.domain.model;

import br.com.senac.biblioteca.domain.exception.RegraNegocioException;

/**
 * Regra de negócio do domínio: cada leitor pode ter no máximo
 * 2 livros emprestados ao mesmo tempo.
 */
public final class PoliticaEmprestimo {

    public static final int LIMITE_POR_LEITOR = 2;

    private PoliticaEmprestimo() {
    }

    public static void validarLimite(String leitor, long emprestimosAtivos) {
        if (emprestimosAtivos >= LIMITE_POR_LEITOR) {
            throw new RegraNegocioException("O leitor " + leitor + " já possui "
                    + emprestimosAtivos + " livros emprestados (limite: " + LIMITE_POR_LEITOR + ").");
        }
    }
}
