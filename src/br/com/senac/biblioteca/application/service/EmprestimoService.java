package br.com.senac.biblioteca.application.service;

import br.com.senac.biblioteca.application.port.in.DevolverLivroUseCase;
import br.com.senac.biblioteca.application.port.in.EmprestarLivroUseCase;
import br.com.senac.biblioteca.application.port.out.LivroRepositoryPort;
import br.com.senac.biblioteca.domain.exception.RegraNegocioException;
import br.com.senac.biblioteca.domain.model.Livro;
import br.com.senac.biblioteca.domain.model.PoliticaEmprestimo;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Casos de uso de empréstimo e devolução.
 * Recebe um Clock para que os testes possam controlar a data (ex.: simular atraso).
 */
public class EmprestimoService implements EmprestarLivroUseCase, DevolverLivroUseCase {

    private final LivroRepositoryPort repositorio;
    private final Clock relogio;

    public EmprestimoService(LivroRepositoryPort repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    @Override
    public Livro emprestar(String isbn, String leitor) {
        Livro livro = buscarObrigatorio(isbn);

        long ativos = repositorio.listarTodos().stream()
                .filter(l -> l.estaEmprestadoPara(leitor))
                .count();
        PoliticaEmprestimo.validarLimite(leitor, ativos); // regra do domínio

        livro.emprestar(leitor, LocalDate.now(relogio));  // regra do domínio
        repositorio.salvar(livro);                        // porta de saída
        return livro;
    }

    @Override
    public ResultadoDevolucao devolver(String isbn) {
        Livro livro = buscarObrigatorio(isbn);
        LocalDate hoje = LocalDate.now(relogio);

        long atraso = livro.diasDeAtraso(hoje);
        BigDecimal multa = livro.devolver(hoje);
        repositorio.salvar(livro);
        return new ResultadoDevolucao(livro, atraso, multa);
    }

    private Livro buscarObrigatorio(String isbn) {
        return repositorio.buscarPorIsbn(Livro.normalizarIsbn(isbn))
                .orElseThrow(() -> new RegraNegocioException(
                        "Livro não encontrado para o ISBN " + Livro.normalizarIsbn(isbn) + "."));
    }
}
