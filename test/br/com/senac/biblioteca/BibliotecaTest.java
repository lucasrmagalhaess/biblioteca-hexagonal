package br.com.senac.biblioteca;

import br.com.senac.biblioteca.adapters.out.arquivo.LivroRepositoryEmArquivo;
import br.com.senac.biblioteca.adapters.out.memoria.LivroRepositoryEmMemoria;
import br.com.senac.biblioteca.application.port.in.DevolverLivroUseCase.ResultadoDevolucao;
import br.com.senac.biblioteca.application.port.out.LivroRepositoryPort;
import br.com.senac.biblioteca.application.service.AcervoService;
import br.com.senac.biblioteca.application.service.EmprestimoService;
import br.com.senac.biblioteca.domain.exception.RegraNegocioException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Supplier;

/**
 * Testes sem framework (Java puro), para rodar sem instalar dependências.
 *
 * O MESMO conjunto de testes é executado duas vezes: uma com o adaptador em memória
 * e outra com o adaptador em arquivo. Se tudo passa nos dois, fica demonstrado que
 * trocar o adaptador de saída não altera o comportamento do núcleo.
 */
public class BibliotecaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final Clock DIA_01 = relogio("2026-11-01");
    private static final Clock DIA_08 = relogio("2026-11-08"); // dentro do prazo
    private static final Clock DIA_11 = relogio("2026-11-11"); // 3 dias de atraso

    private static int aprovados = 0;
    private static int reprovados = 0;

    public static void main(String[] args) throws IOException {
        executarSuite("Adaptador em MEMÓRIA", LivroRepositoryEmMemoria::new);
        executarSuite("Adaptador em ARQUIVO", BibliotecaTest::novoRepositorioEmArquivo);

        System.out.printf("%nResultado: %d aprovado(s), %d reprovado(s)%n", aprovados, reprovados);
        if (reprovados > 0) {
            System.exit(1);
        }
    }

    private static void executarSuite(String nome, Supplier<LivroRepositoryPort> fabrica) {
        System.out.println("\n### " + nome);

        teste("cadastra livro e lista no acervo", () -> {
            AcervoService acervo = new AcervoService(fabrica.get());
            acervo.cadastrar("111", "Livro A", "Autora A");
            verificar(acervo.listarTodos().size() == 1, "deveria haver 1 livro");
        });

        teste("não permite ISBN duplicado", () -> {
            AcervoService acervo = new AcervoService(fabrica.get());
            acervo.cadastrar("111", "Livro A", "Autora A");
            esperarErro(() -> acervo.cadastrar(" 111 ", "Outro", "Outro autor"));
        });

        teste("não permite título vazio", () -> {
            AcervoService acervo = new AcervoService(fabrica.get());
            esperarErro(() -> acervo.cadastrar("222", "", "Autor"));
        });

        teste("empresta livro disponível com prazo de 7 dias", () -> {
            LivroRepositoryPort repo = fabrica.get();
            new AcervoService(repo).cadastrar("111", "Livro A", "Autora A");
            var livro = new EmprestimoService(repo, DIA_01).emprestar("111", "Maria");
            verificar(livro.getDataPrevistaDevolucao().toString().equals("2026-11-08"), "prazo incorreto");
            verificar(new AcervoService(repo).listarDisponiveis().isEmpty(), "livro deveria estar indisponível");
        });

        teste("não empresta livro já emprestado", () -> {
            LivroRepositoryPort repo = fabrica.get();
            new AcervoService(repo).cadastrar("111", "Livro A", "Autora A");
            EmprestimoService servico = new EmprestimoService(repo, DIA_01);
            servico.emprestar("111", "Maria");
            esperarErro(() -> servico.emprestar("111", "João"));
        });

        teste("respeita o limite de 2 livros por leitor", () -> {
            LivroRepositoryPort repo = fabrica.get();
            AcervoService acervo = new AcervoService(repo);
            acervo.cadastrar("111", "Livro A", "Autor");
            acervo.cadastrar("222", "Livro B", "Autor");
            acervo.cadastrar("333", "Livro C", "Autor");
            EmprestimoService servico = new EmprestimoService(repo, DIA_01);
            servico.emprestar("111", "Maria");
            servico.emprestar("222", "Maria");
            esperarErro(() -> servico.emprestar("333", "Maria"));
        });

        teste("devolução no prazo não gera multa", () -> {
            LivroRepositoryPort repo = fabrica.get();
            new AcervoService(repo).cadastrar("111", "Livro A", "Autora A");
            new EmprestimoService(repo, DIA_01).emprestar("111", "Maria");
            ResultadoDevolucao r = new EmprestimoService(repo, DIA_08).devolver("111");
            verificar(r.multa().compareTo(BigDecimal.ZERO) == 0, "multa deveria ser zero");
        });

        teste("devolução com 3 dias de atraso gera multa de R$ 6,00", () -> {
            LivroRepositoryPort repo = fabrica.get();
            new AcervoService(repo).cadastrar("111", "Livro A", "Autora A");
            new EmprestimoService(repo, DIA_01).emprestar("111", "Maria");
            ResultadoDevolucao r = new EmprestimoService(repo, DIA_11).devolver("111");
            verificar(r.diasDeAtraso() == 3, "deveria ter 3 dias de atraso");
            verificar(r.multa().compareTo(new BigDecimal("6.00")) == 0, "multa deveria ser R$ 6,00");
        });

        teste("não devolve livro que não está emprestado", () -> {
            LivroRepositoryPort repo = fabrica.get();
            new AcervoService(repo).cadastrar("111", "Livro A", "Autora A");
            esperarErro(() -> new EmprestimoService(repo, DIA_01).devolver("111"));
        });
    }

    // ---------- utilitários ----------

    private static LivroRepositoryPort novoRepositorioEmArquivo() {
        try {
            File temp = File.createTempFile("livros-teste", ".csv");
            temp.deleteOnExit();
            return new LivroRepositoryEmArquivo(temp.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Clock relogio(String data) {
        return Clock.fixed(Instant.parse(data + "T12:00:00Z"), ZONA);
    }

    private static void teste(String descricao, Runnable corpo) {
        try {
            corpo.run();
            aprovados++;
            System.out.println("  [OK]    " + descricao);
        } catch (AssertionError | RuntimeException e) {
            reprovados++;
            System.out.println("  [FALHA] " + descricao + " -> " + e.getMessage());
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static void esperarErro(Runnable acao) {
        try {
            acao.run();
        } catch (RegraNegocioException esperado) {
            return;
        }
        throw new AssertionError("deveria ter lançado RegraNegocioException");
    }
}
