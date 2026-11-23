package br.com.senac.biblioteca.application.port.in;

import br.com.senac.biblioteca.domain.model.Livro;

/** Porta de entrada: cadastrar um novo livro no acervo. */
public interface CadastrarLivroUseCase {

    Livro cadastrar(String isbn, String titulo, String autor);
}
