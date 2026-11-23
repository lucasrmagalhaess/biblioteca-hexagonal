# Biblioteca: Arquitetura Hexagonal (Portas e Adaptadores)

Exemplo em **Java puro** (sem frameworks) para a atividade semestral de **Engenharia e Arquitetura de Software (Senac)**.
O sistema controla o acervo de uma biblioteca: cadastro de livros, consultas, empréstimos e devoluções com multa por atraso.

## Integrantes
* Lucas Rodrigues de Magalhaes

## Requisitos
* **Java 17 ou superior** (testado com OpenJDK 21)
* Nenhuma dependência externa, banco de dados ou framework

---

## Regras de negócio

| Regra | Onde está |
| :--- | :--- |
| ISBN e autor são obrigatórios; o título precisa de pelo menos 2 caracteres | `Livro` (construtor) |
| Só é possível emprestar um livro **disponível** | `Livro.emprestar()` |
| O prazo de devolução é de **7 dias** | `Livro.PRAZO_DIAS` |
| Cada dia de atraso gera multa de **R$ 2,00** | `Livro.devolver()` |
| Cada leitor pode ter no máximo **2 livros** emprestados | `PoliticaEmprestimo` |
| Não é possível cadastrar dois livros com o mesmo ISBN | `AcervoService.cadastrar()` |

---

## Estrutura de pacotes

```text
biblioteca-hexagonal/
├── src/br/com/senac/biblioteca/
│   ├── domain/                                  # NÚCLEO: regras de negócio puras
│   │   ├── model/
│   │   │   ├── Livro.java                       # Entidade (empréstimo, devolução, multa)
│   │   │   └── PoliticaEmprestimo.java          # Limite de livros por leitor
│   │   └── exception/
│   │       └── RegraNegocioException.java
│   │
│   ├── application/                             # NÚCLEO: casos de uso e contratos
│   │   ├── port/
│   │   │   ├── in/                              # Portas de entrada (o que o sistema oferece)
│   │   │   │   ├── CadastrarLivroUseCase.java
│   │   │   │   ├── ConsultarAcervoUseCase.java
│   │   │   │   ├── EmprestarLivroUseCase.java
│   │   │   │   └── DevolverLivroUseCase.java
│   │   │   └── out/                             # Porta de saída (o que o sistema precisa)
│   │   │       └── LivroRepositoryPort.java
│   │   └── service/                             # Implementação dos casos de uso
│   │       ├── AcervoService.java
│   │       └── EmprestimoService.java
│   │
│   ├── adapters/                                # EXTERNO: tecnologia
│   │   ├── in/console/
│   │   │   └── ConsoleBibliotecaAdapter.java    # Adaptador de entrada (terminal)
│   │   └── out/
│   │       ├── memoria/
│   │       │   └── LivroRepositoryEmMemoria.java   # Adaptador de saída 1 (RAM)
│   │       └── arquivo/
│   │           └── LivroRepositoryEmArquivo.java   # Adaptador de saída 2 (CSV)
│   │
│   └── Main.java                                # Montagem das dependências
│
└── test/br/com/senac/biblioteca/
    └── BibliotecaTest.java                      # Mesmos testes executados nos 2 adaptadores
```

## Componentes arquiteturais

| Componente | Camada | Classes / interfaces | Responsabilidade |
| :--- | :--- | :--- | :--- |
| Domínio | Núcleo | `Livro`, `PoliticaEmprestimo`, `RegraNegocioException` | Dados e regras de negócio, sem depender de tecnologia |
| Portas de entrada | Núcleo (fronteira) | `CadastrarLivroUseCase`, `ConsultarAcervoUseCase`, `EmprestarLivroUseCase`, `DevolverLivroUseCase` | Interfaces com as operações oferecidas ao mundo externo |
| Casos de uso | Núcleo | `AcervoService`, `EmprestimoService` | Implementam as portas de entrada e orquestram domínio e porta de saída |
| Porta de saída | Núcleo (fronteira) | `LivroRepositoryPort` | Contrato de persistência exigido pelo núcleo |
| Adaptador de entrada | Externo | `ConsoleBibliotecaAdapter` | Lê o terminal e chama as portas de entrada |
| Adaptadores de saída | Externo | `LivroRepositoryEmMemoria`, `LivroRepositoryEmArquivo` | Duas implementações da mesma porta de saída |
| Composição | Externo | `Main` | Escolhe o adaptador e injeta as dependências |

**Direção das dependências:** os adaptadores dependem do núcleo; o núcleo nunca importa nada de `adapters`.
O `EmprestimoService` recebe a interface `LivroRepositoryPort` no construtor (inversão de dependência).

## Fluxo de uma operação completa (empréstimo)

1. **Entrada do usuário:** `ConsoleBibliotecaAdapter` lê o ISBN e o nome do leitor.
2. **Caso de uso:** o adaptador chama `emprestarLivro.emprestar(isbn, leitor)` pela porta de entrada.
3. **Regra de negócio:** `EmprestimoService` busca o livro, verifica o limite com `PoliticaEmprestimo` e chama `livro.emprestar(...)`, que confere a disponibilidade e calcula o prazo.
4. **Porta de saída:** o serviço chama `repositorio.salvar(livro)` pela interface `LivroRepositoryPort`.
5. **Retorno:** o livro atualizado volta ao console, que mostra a data de devolução. Se alguma regra falhar, uma `RegraNegocioException` sobe até o console e a mensagem é exibida.

---

## Como compilar e executar

Na pasta raiz do projeto (`biblioteca-hexagonal`):

**Linux / macOS**
```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out br.com.senac.biblioteca.Main
```

**Windows (PowerShell)**
```powershell
mkdir out -Force
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out br.com.senac.biblioteca.Main
```

> Se os acentos aparecerem trocados no terminal do Windows, rode `chcp 65001` antes e use
> `java -Dstdout.encoding=UTF-8 -cp out br.com.senac.biblioteca.Main`.

Quando o acervo está vazio, o programa cadastra 3 livros de exemplo para facilitar a demonstração.

## Como executar os testes

```bash
javac -encoding UTF-8 -d out $(find src test -name "*.java")
java -cp out br.com.senac.biblioteca.BibliotecaTest
```
(No PowerShell, troque `$(find src test -name "*.java")` por `(Get-ChildItem -Recurse src,test -Filter *.java).FullName`.)

Os testes não usam framework. O mesmo conjunto de 9 cenários roda **duas vezes**: com o adaptador em memória
e com o adaptador em arquivo (18 testes ao todo). O `EmprestimoService` recebe um `Clock` fixo, o que permite
simular atrasos sem esperar dias de verdade.

---

## Como alternar os adaptadores de saída

A troca acontece **somente na montagem da aplicação** (`Main.java`). Nenhuma linha do domínio, dos casos de uso
ou das portas é alterada.

1. **Pelo menu ao iniciar:** digite `1` (memória) ou `2` (arquivo `livros.csv`).
2. **Por argumento:**
   ```bash
   java -cp out br.com.senac.biblioteca.Main memoria
   java -cp out br.com.senac.biblioteca.Main arquivo
   ```
3. **No código (`Main.java`):**
   ```java
   LivroRepositoryPort repositorio = new LivroRepositoryEmMemoria();
   // LivroRepositoryPort repositorio = new LivroRepositoryEmArquivo("livros.csv");

   AcervoService acervo = new AcervoService(repositorio);
   EmprestimoService emprestimos = new EmprestimoService(repositorio, Clock.systemDefaultZone());
   ```

Com o adaptador de arquivo, os dados ficam salvos em `livros.csv` e continuam lá na próxima execução.

## Limitações conhecidas
* O adaptador de arquivo regrava o CSV inteiro a cada alteração, o que serve para um exemplo didático, mas não para grandes volumes.
* O caractere `;` é usado como separador e, por isso, é trocado por `,` dentro dos textos.
* Não há controle de acesso simultâneo ao arquivo.
