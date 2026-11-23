package br.com.senac.biblioteca.application.service;

import br.com.senac.biblioteca.application.port.in.CadastrarLivroUseCase;
import br.com.senac.biblioteca.application.port.in.ConsultarAcervoUseCase;
import br.com.senac.biblioteca.application.port.out.LivroRepositoryPort;
import br.com.senac.biblioteca.domain.exception.RegraNegocioException;
import br.com.senac.biblioteca.domain.model.Livro;

import java.util.List;
import java.util.Optional;

/**
 * Casos de uso de cadastro e consulta do acervo.
 * Depende apenas da porta de saída (interface), nunca de um adaptador concreto.
 */
public class AcervoService implements CadastrarLivroUseCase, ConsultarAcervoUseCase {

    private final LivroRepositoryPort repositorio;

    public AcervoService(LivroRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Livro cadastrar(String isbn, String titulo, String autor) {
        if (repositorio.buscarPorIsbn(Livro.normalizarIsbn(isbn)).isPresent()) {
            throw new RegraNegocioException("Já existe um livro com o ISBN " + Livro.normalizarIsbn(isbn) + ".");
        }
        Livro livro = new Livro(isbn, titulo, autor); // a entidade valida os próprios dados
        repositorio.salvar(livro);
        return livro;
    }

    @Override
    public List<Livro> listarTodos() {
        return repositorio.listarTodos();
    }

    @Override
    public List<Livro> listarDisponiveis() {
        return repositorio.listarTodos().stream()
                .filter(Livro::isDisponivel)
                .toList();
    }

    @Override
    public Optional<Livro> buscarPorIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return Optional.empty();
        }
        return repositorio.buscarPorIsbn(Livro.normalizarIsbn(isbn));
    }
}
