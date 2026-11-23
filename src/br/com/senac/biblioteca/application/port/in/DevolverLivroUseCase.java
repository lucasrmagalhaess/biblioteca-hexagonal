package br.com.senac.biblioteca.application.port.in;

import br.com.senac.biblioteca.domain.model.Livro;

import java.math.BigDecimal;

/** Porta de entrada: devolver um livro emprestado. */
public interface DevolverLivroUseCase {

    ResultadoDevolucao devolver(String isbn);

    /** Dados retornados ao adaptador de entrada após a devolução. */
    record ResultadoDevolucao(Livro livro, long diasDeAtraso, BigDecimal multa) {
    }
}
