package br.com.senac.biblioteca.application.port.in;

import br.com.senac.biblioteca.domain.model.Livro;

/** Porta de entrada: emprestar um livro para um leitor. */
public interface EmprestarLivroUseCase {

    Livro emprestar(String isbn, String leitor);
}
