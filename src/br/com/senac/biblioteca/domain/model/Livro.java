package br.com.senac.biblioteca.domain.model;

import br.com.senac.biblioteca.domain.exception.RegraNegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entidade de domínio. Concentra os dados do livro e as regras de empréstimo:
 * - só é possível emprestar um livro disponível;
 * - o prazo de devolução é de 7 dias;
 * - cada dia de atraso gera multa de R$ 2,00.
 *
 * Não depende de nenhuma tecnologia externa (console, arquivo, banco de dados).
 */
public class Livro {

    public static final int PRAZO_DIAS = 7;
    public static final BigDecimal MULTA_DIARIA = new BigDecimal("2.00");

    private final String isbn;
    private final String titulo;
    private final String autor;

    private String leitorAtual;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;

    public Livro(String isbn, String titulo, String autor) {
        if (isbn == null || isbn.isBlank()) {
            throw new RegraNegocioException("O ISBN é obrigatório.");
        }
        if (titulo == null || titulo.trim().length() < 2) {
            throw new RegraNegocioException("O título deve ter pelo menos 2 caracteres.");
        }
        if (autor == null || autor.isBlank()) {
            throw new RegraNegocioException("O autor é obrigatório.");
        }
        this.isbn = normalizarIsbn(isbn);
        this.titulo = titulo.trim();
        this.autor = autor.trim();
    }

    /**
     * Recria um livro já existente (usado pelos adaptadores de persistência).
     */
    public static Livro reconstituir(String isbn, String titulo, String autor,
                                     String leitorAtual, LocalDate dataEmprestimo,
                                     LocalDate dataPrevistaDevolucao) {
        Livro livro = new Livro(isbn, titulo, autor);
        livro.leitorAtual = leitorAtual;
        livro.dataEmprestimo = dataEmprestimo;
        livro.dataPrevistaDevolucao = dataPrevistaDevolucao;
        return livro;
    }

    public static String normalizarIsbn(String isbn) {
        return isbn == null ? "" : isbn.trim().toUpperCase();
    }

    /** Regra de negócio: empresta o livro se ele estiver disponível. */
    public void emprestar(String leitor, LocalDate hoje) {
        if (leitor == null || leitor.trim().length() < 2) {
            throw new RegraNegocioException("Informe o nome do leitor (mínimo 2 caracteres).");
        }
        if (!isDisponivel()) {
            throw new RegraNegocioException("O livro \"" + titulo + "\" já está emprestado para "
                    + leitorAtual + " até " + dataPrevistaDevolucao + ".");
        }
        this.leitorAtual = leitor.trim();
        this.dataEmprestimo = hoje;
        this.dataPrevistaDevolucao = hoje.plusDays(PRAZO_DIAS);
    }

    /** Regra de negócio: devolve o livro e retorna o valor da multa por atraso. */
    public BigDecimal devolver(LocalDate hoje) {
        if (isDisponivel()) {
            throw new RegraNegocioException("O livro \"" + titulo + "\" não está emprestado.");
        }
        BigDecimal multa = MULTA_DIARIA.multiply(BigDecimal.valueOf(diasDeAtraso(hoje)));
        this.leitorAtual = null;
        this.dataEmprestimo = null;
        this.dataPrevistaDevolucao = null;
        return multa;
    }

    public long diasDeAtraso(LocalDate hoje) {
        if (isDisponivel() || !hoje.isAfter(dataPrevistaDevolucao)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dataPrevistaDevolucao, hoje);
    }

    public boolean isDisponivel() {
        return leitorAtual == null;
    }

    public boolean estaEmprestadoPara(String leitor) {
        return leitorAtual != null && leitor != null && leitorAtual.equalsIgnoreCase(leitor.trim());
    }

    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getLeitorAtual() { return leitorAtual; }
    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public LocalDate getDataPrevistaDevolucao() { return dataPrevistaDevolucao; }

    @Override
    public String toString() {
        String situacao = isDisponivel()
                ? "DISPONÍVEL"
                : "EMPRESTADO para " + leitorAtual + " (devolver até " + dataPrevistaDevolucao + ")";
        return String.format("[%s] %s - %s | %s", isbn, titulo, autor, situacao);
    }
}
