package br.com.senac.biblioteca.application.port.in;

import br.com.senac.biblioteca.domain.model.Livro;

import java.util.List;
import java.util.Optional;

/** Porta de entrada: consultas ao acervo. */
public interface ConsultarAcervoUseCase {

    List<Livro> listarTodos();

    List<Livro> listarDisponiveis();

    Optional<Livro> buscarPorIsbn(String isbn);
}
