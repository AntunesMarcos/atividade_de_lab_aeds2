import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

	/** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente no vetor */
    static int quantosProdutos = 0;

    static ABB<String, Produto> produtosCadastradosPorNome;
    
    static ABB<Integer, Produto> produtosCadastradosPorId;
    
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
    
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        
    	T valor;
        
    	System.out.println(mensagem);
    	try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
        		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    /** 
     * Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário (-1 se a entrada não for um número).
    */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar produto, por nome");
        System.out.println("3 - Procurar produto, por id");
        System.out.println("4 - Remover produto, por nome");
        System.out.println("5 - Remover produto, por id");
        System.out.println("6 - Recortar a lista de produtos, por nome");
        System.out.println("7 - Recortar a lista de produtos, por id");
        System.out.println("0 - Finalizar");
        
        Integer opcao = lerOpcao("Digite sua opção: ", Integer.class);
        return (opcao == null) ? -1 : opcao;
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna uma ávore de produtos. Arquivo-texto no formato
     * N  (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna uma árvore vazia em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Uma árvore com os produtos carregados, ou vazia em caso de problemas de leitura.
     */
    static <K> ABB<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
    	return lerProdutos(nomeArquivoDados, extratorDeChave, null);
    }
    
    /**
     * Mesma leitura do método acima, mas permitindo escolher o comparador que organiza a árvore
     * (null = ordem natural das chaves). Usado, por exemplo, para uma árvore de nomes que ignora maiúsculas/minúsculas.
     */
    static <K> ABB<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave, Comparator<K> comparador) {
    	
    	Scanner arquivo = null;
    	int numProdutos;
    	String linha;
    	Produto produto;
    	ABB<K, Produto> produtosCadastrados;
    	
    	try {
    		arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
    		
    		numProdutos = Integer.parseInt(arquivo.nextLine());
    		produtosCadastrados = new ABB<K, Produto>(comparador);
    		
    		for (int i = 0; i < numProdutos; i++) {
    			linha = arquivo.nextLine();
    			produto = Produto.criarDoTexto(linha);
    			K chave = extratorDeChave.apply(produto);
    			produtosCadastrados.inserir(chave, produto);
    		}
    		quantosProdutos = numProdutos;
    		
    	} catch (IOException excecaoArquivo) {
    		produtosCadastrados = new ABB<K, Produto>(comparador); // árvore vazia, como descrito acima
    		quantosProdutos = 0;
    	} finally {
    		if (arquivo != null)
    			arquivo.close();
    	}
    	
    	return produtosCadastrados;
    }
    
    /** Imprime a quantidade de comparações e o tempo gastos na última operação da árvore. */
    private static void imprimirMedicoes(IMedicao medicao) {
    	System.out.println("Comparações realizadas: " + medicao.getComparacoes());
    	System.out.printf("Tempo de execução: %.4f ms%n", medicao.getTempo());
    }
    
    /** Localiza um produto na árvore a partir da chave informada. Retorna null se não encontrar. */
    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {
    	
    	try {
    		return produtosCadastrados.pesquisar(procurado);
    	} catch (NoSuchElementException excecao) {
    		return null;
    	}
    }
    
    /** Localiza um produto na árvore de produtos organizados por id, a partir do código de produto informado pelo usuário, e o retorna. 
     *  Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        
    	Integer id = lerOpcao("Digite o id do produto: ", Integer.class);
    	if (id == null) {
    		System.out.println("Id inválido.");
    		return null;
    	}
    	
    	Produto produto = localizarProduto(produtosCadastrados, id);
    	imprimirMedicoes(produtosCadastrados);
    	return produto;
    }
    
    /** Localiza um produto na árvore de produtos organizados por nome, a partir do nome de produto informado pelo usuário, e o retorna. 
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        
    	System.out.println("Digite o nome do produto: ");
    	String nome = teclado.nextLine().trim();
    	
    	Produto produto = localizarProduto(produtosCadastrados, nome);
    	imprimirMedicoes(produtosCadastrados);
    	return produto;
    }
    
    private static void mostrarProduto(Produto produto) {
    	
        cabecalho();
        StringBuilder  mensagem = new StringBuilder("Produto não encontrado.\n");
        
        if (produto != null) {
            mensagem = new StringBuilder(String.format("%s\n", produto));            
        }

        System.out.println(mensagem.toString());
    }

    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static <K> void listarTodosOsProdutos(ABB<K, Produto> produtosCadastrados) {
    	
        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        System.out.println(produtosCadastrados.toString());
    }
    
    /** Localiza e remove um produto da árvore de produtos organizados por id, a partir do código de produto informado pelo usuário, e o retorna. 
     *  Em caso de não encontrar o produto, retorna null.
     *  O produto também é retirado da árvore organizada por nome, para as duas árvores continuarem iguais. */
    static Produto removerProdutoId(ABB<Integer, Produto> produtosCadastrados) {
    	
    	Integer id = lerOpcao("Digite o id do produto a ser removido: ", Integer.class);
    	if (id == null) {
    		System.out.println("Id inválido.");
    		return null;
    	}
    	
    	Produto removido = removerProduto(produtosCadastrados, id);
    	if (removido != null) {
    		removerProduto(produtosCadastradosPorNome, removido.descricao);
    		quantosProdutos--;
    	}
    	return removido;
    }

     /** Localiza e remove um produto na árvore de produtos organizados por nome, a partir do nome de produto informado pelo usuário, e o retorna. 
      *  A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null.
      *  O produto também é retirado da árvore organizada por id, para as duas árvores continuarem iguais. */
    static Produto removerProdutoNome(ABB<String, Produto> produtosCadastrados) {
    	
    	System.out.println("Digite o nome do produto a ser removido: ");
    	String nome = teclado.nextLine().trim();
    	
    	Produto removido = removerProduto(produtosCadastrados, nome);
    	if (removido != null) {
    		removerProduto(produtosCadastradosPorId, removido.idProduto);
    		quantosProdutos--;
    	}
    	return removido;
    }

    /** Remove da árvore o produto com a chave informada e o retorna. Retorna null se a chave não existir. */
    static <K> Produto removerProduto(ABB<K, Produto> produtosCadastrados, K chave){
    	
    	try {
    		return produtosCadastrados.remover(chave);
    	} catch (NoSuchElementException excecao) {
    		return null;
    	}
    }
    
    /** Recorte genérico: lista, em ordem, os produtos cujas chaves estão entre deOnde e ateOnde (inclusive). */
    private static <K> void recortarProduto(ABB<K, Produto> produtosCadastrados, K deOnde, K ateOnde) {
    	
    	cabecalho();
    	Lista<Produto> recorte = produtosCadastrados.recortar(deOnde, ateOnde);
    	
    	System.out.println("\nPRODUTOS ENTRE \"" + deOnde + "\" E \"" + ateOnde + "\":");
    	System.out.println(recorte);
    }
    
    private static void recortarProdutosNome(ABB<String, Produto> produtosCadastrados) {
    	
    	System.out.println("Digite o nome inicial do recorte: ");
    	String deOnde = teclado.nextLine().trim();
    	System.out.println("Digite o nome final do recorte: ");
    	String ateOnde = teclado.nextLine().trim();
    	
    	recortarProduto(produtosCadastrados, deOnde, ateOnde);
    }
     
    private static void recortarProdutosId(ABB<Integer, Produto> produtosCadastrados) {
    	
    	Integer deOnde = lerOpcao("Digite o id inicial do recorte: ", Integer.class);
    	Integer ateOnde = lerOpcao("Digite o id final do recorte: ", Integer.class);
    	
    	if ((deOnde == null) || (ateOnde == null)) {
    		System.out.println("Id inválido.");
    		return;
    	}
    	
    	recortarProduto(produtosCadastrados, deOnde, ateOnde);
    }
    
    public static void main(String[] args) {
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        // árvore de nomes com comparador que ignora maiúsculas/minúsculas (busca "não sensível ao caso")
        produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao), String.CASE_INSENSITIVE_ORDER);
        produtosCadastradosPorId = new ABB<Integer, Produto>(produtosCadastradosPorNome, (p -> p.idProduto));
        
        int opcao = -1;
      
        do{
        	opcao = menu();
            switch (opcao) {
            case 1 -> listarTodosOsProdutos(produtosCadastradosPorNome);
            case 2 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
            case 3 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
            case 4 -> mostrarProduto(removerProdutoNome(produtosCadastradosPorNome));
        	case 5 -> mostrarProduto(removerProdutoId(produtosCadastradosPorId));
        	case 6 -> recortarProdutosNome(produtosCadastradosPorNome); 
        	case 7 -> recortarProdutosId(produtosCadastradosPorId); 
            case 0 -> System.out.println("FLW VLW OBG VLT SMP.");
            default -> System.out.println("Opção inválida.");
            }
            pausa();
        } while (opcao != 0);       

        teclado.close();    
    }
}