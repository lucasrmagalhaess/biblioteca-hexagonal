package br.com.senac.biblioteca;

import br.com.senac.biblioteca.adapters.in.console.ConsoleBibliotecaAdapter;
import br.com.senac.biblioteca.adapters.out.arquivo.LivroRepositoryEmArquivo;
import br.com.senac.biblioteca.adapters.out.memoria.LivroRepositoryEmMemoria;
import br.com.senac.biblioteca.application.port.out.LivroRepositoryPort;
import br.com.senac.biblioteca.application.service.AcervoService;
import br.com.senac.biblioteca.application.service.EmprestimoService;

import java.time.Clock;
import java.util.Scanner;

/**
 * Ponto de montagem (composição) da aplicação.
 * É o ÚNICO lugar que conhece os adaptadores concretos e os conecta ao núcleo.
 *
 * Uso: java -cp out br.com.senac.biblioteca.Main [memoria|arquivo]
 * Sem argumento, o programa pergunta qual adaptador usar.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String escolha = args.length > 0 ? args[0] : perguntarAdaptador(scanner);

        // 1) Escolhe o adaptador de saída
        LivroRepositoryPort repositorio;
        if ("2".equals(escolha) || "arquivo".equalsIgnoreCase(escolha)) {
            repositorio = new LivroRepositoryEmArquivo("livros.csv");
            System.out.println(">> Adaptador de saída: ARQUIVO (livros.csv)");
        } else {
            repositorio = new LivroRepositoryEmMemoria();
            System.out.println(">> Adaptador de saída: MEMÓRIA");
        }

        // 2) Cria o núcleo, que recebe apenas a interface (porta de saída)
        AcervoService acervo = new AcervoService(repositorio);
        EmprestimoService emprestimos = new EmprestimoService(repositorio, Clock.systemDefaultZone());

        if (acervo.listarTodos().isEmpty()) {
            carregarExemplos(acervo);
        }

        // 3) Conecta o adaptador de entrada às portas de entrada
        new ConsoleBibliotecaAdapter(acervo, acervo, emprestimos, emprestimos, scanner).iniciar();
    }

    private static String perguntarAdaptador(Scanner scanner) {
        System.out.println("Escolha o adaptador de persistência (porta de saída):");
        System.out.println("1 - Memória RAM");
        System.out.println("2 - Arquivo CSV (livros.csv)");
        System.out.print("Opção: ");
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "1";
    }

    private static void carregarExemplos(AcervoService acervo) {
        acervo.cadastrar("978-85-7522-001", "Arquitetura Limpa", "Robert C. Martin");
        acervo.cadastrar("978-85-7522-002", "Domain-Driven Design", "Eric Evans");
        acervo.cadastrar("978-85-7522-003", "Padrões de Projeto", "Gamma, Helm, Johnson e Vlissides");
        System.out.println(">> Acervo vazio: 3 livros de exemplo foram cadastrados.");
    }
}
