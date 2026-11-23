package br.com.senac.biblioteca.adapters.in.console;

import br.com.senac.biblioteca.application.port.in.CadastrarLivroUseCase;
import br.com.senac.biblioteca.application.port.in.ConsultarAcervoUseCase;
import br.com.senac.biblioteca.application.port.in.DevolverLivroUseCase;
import br.com.senac.biblioteca.application.port.in.DevolverLivroUseCase.ResultadoDevolucao;
import br.com.senac.biblioteca.application.port.in.EmprestarLivroUseCase;
import br.com.senac.biblioteca.domain.exception.RegraNegocioException;
import br.com.senac.biblioteca.domain.model.Livro;

import java.util.List;
import java.util.Scanner;

/**
 * Adaptador de entrada: traduz o que o usuário digita no terminal
 * em chamadas às portas de entrada (casos de uso). Não contém regra de negócio.
 */
public class ConsoleBibliotecaAdapter {

    private final CadastrarLivroUseCase cadastrarLivro;
    private final ConsultarAcervoUseCase consultarAcervo;
    private final EmprestarLivroUseCase emprestarLivro;
    private final DevolverLivroUseCase devolverLivro;
    private final Scanner scanner;

    public ConsoleBibliotecaAdapter(CadastrarLivroUseCase cadastrarLivro,
                                    ConsultarAcervoUseCase consultarAcervo,
                                    EmprestarLivroUseCase emprestarLivro,
                                    DevolverLivroUseCase devolverLivro,
                                    Scanner scanner) {
        this.cadastrarLivro = cadastrarLivro;
        this.consultarAcervo = consultarAcervo;
        this.emprestarLivro = emprestarLivro;
        this.devolverLivro = devolverLivro;
        this.scanner = scanner;
    }

    public void iniciar() {
        int opcao = -1;
        while (opcao != 0) {
            exibirMenu();
            if (!scanner.hasNextLine()) {
                break;
            }
            try {
                opcao = Integer.parseInt(scanner.nextLine().trim());
                executar(opcao);
            } catch (NumberFormatException e) {
                System.out.println("Erro: digite apenas o número da opção.");
            } catch (RegraNegocioException e) {
                System.out.println("Regra de negócio: " + e.getMessage());
            }
        }
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("=== BIBLIOTECA (ARQUITETURA HEXAGONAL) ===");
        System.out.println("1 - Cadastrar livro");
        System.out.println("2 - Listar acervo");
        System.out.println("3 - Listar livros disponíveis");
        System.out.println("4 - Buscar livro por ISBN");
        System.out.println("5 - Emprestar livro");
        System.out.println("6 - Devolver livro");
        System.out.println("0 - Sair");
        System.out.print("Opção: ");
    }

    private void executar(int opcao) {
        switch (opcao) {
            case 1 -> cadastrar();
            case 2 -> imprimir(consultarAcervo.listarTodos(), "Nenhum livro cadastrado.");
            case 3 -> imprimir(consultarAcervo.listarDisponiveis(), "Nenhum livro disponível.");
            case 4 -> buscar();
            case 5 -> emprestar();
            case 6 -> devolver();
            case 0 -> System.out.println("Encerrando...");
            default -> System.out.println("Opção inválida.");
        }
    }

    private void cadastrar() {
        String isbn = perguntar("ISBN: ");
        String titulo = perguntar("Título: ");
        String autor = perguntar("Autor: ");
        Livro livro = cadastrarLivro.cadastrar(isbn, titulo, autor);
        System.out.println("Livro cadastrado: " + livro);
    }

    private void buscar() {
        String isbn = perguntar("ISBN: ");
        consultarAcervo.buscarPorIsbn(isbn).ifPresentOrElse(
                livro -> System.out.println(livro),
                () -> System.out.println("Livro não encontrado."));
    }

    private void emprestar() {
        String isbn = perguntar("ISBN do livro: ");
        String leitor = perguntar("Nome do leitor: ");
        Livro livro = emprestarLivro.emprestar(isbn, leitor);
        System.out.println("Empréstimo realizado! Devolver até " + livro.getDataPrevistaDevolucao() + ".");
    }

    private void devolver() {
        String isbn = perguntar("ISBN do livro: ");
        ResultadoDevolucao resultado = devolverLivro.devolver(isbn);
        if (resultado.diasDeAtraso() == 0) {
            System.out.println("Devolução registrada sem atraso. Obrigado!");
        } else {
            System.out.printf("Devolução com %d dia(s) de atraso. Multa: R$ %.2f%n",
                    resultado.diasDeAtraso(), resultado.multa());
        }
    }

    private void imprimir(List<Livro> livros, String mensagemVazia) {
        if (livros.isEmpty()) {
            System.out.println(mensagemVazia);
            return;
        }
        livros.forEach(System.out::println);
    }

    private String perguntar(String rotulo) {
        System.out.print(rotulo);
        return scanner.hasNextLine() ? scanner.nextLine() : "";
    }
}
