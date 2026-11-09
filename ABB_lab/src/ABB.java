import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ABB<K, V> implements IMapeamento<K, V> {

	// atributos
	private No<K, V> raiz;
	private Comparator<K> comparador;
	private int tamanho;
	private V itemRemovido; // auxiliar: guarda o item retirado durante a remoção recursiva

	private long comparacoes;
	private long inicio;
	private long termino;

	/**
	 * Construtor da classe: árvore vazia ordenada pela ordem natural das chaves (Comparable).
	 */
	public ABB() {
		this(null);
	}

	/**
	 * Construtor da classe.
	 * @param comparador o comparador a ser utilizado para organizar os elementos da árvore.
	 *                   Se for null, será usada a ordem natural das chaves.
	 */
	public ABB(Comparator<K> comparador) {
		this.comparador = (comparador != null) ? comparador : ordemNatural();
		this.raiz = null;
		this.tamanho = 0;
		zerarMedicoes();
	}

	/**
	 * Construtor que cria uma nova árvore com os mesmos itens de outra, mas indexada por outra chave.
	 * Ex.: árvore de produtos por nome -> árvore dos mesmos produtos por id.
	 * Os itens são inseridos em pré-ordem para que a nova árvore não degenere em uma lista.
	 * @param original árvore de onde os itens serão copiados.
	 * @param extratorChave função que obtém, a partir de um item, a sua chave na nova árvore.
	 */
	public <T> ABB(ABB<T, V> original, Function<V, K> extratorChave) {
		this(null);
		copiarEmPreOrdem(original.raiz, extratorChave);
		zerarMedicoes();
	}

	@SuppressWarnings("unchecked")
	private static <T> Comparator<T> ordemNatural() {
		return (a, b) -> ((Comparable<T>) a).compareTo(b);
	}

	private <T> void copiarEmPreOrdem(No<T, V> no, Function<V, K> extratorChave) {
		if (no != null) {
			inserir(extratorChave.apply(no.getItem()), no.getItem());
			copiarEmPreOrdem(no.getEsquerda(), extratorChave);
			copiarEmPreOrdem(no.getDireita(), extratorChave);
		}
	}

	// ---------- medições ----------

	@Override
	public long getComparacoes() {
		return comparacoes;
	}

	@Override
	public double getTempo() {
		return (termino - inicio) / 1_000_000.0; // nanossegundos -> milissegundos (com casas decimais)
	}

	private void iniciarMedicao() {
		comparacoes = 0;
		inicio = System.nanoTime();
	}

	private void terminarMedicao() {
		termino = System.nanoTime();
	}

	private void zerarMedicoes() {
		comparacoes = 0;
		inicio = 0;
		termino = 0;
	}

	/** Único ponto onde chaves são comparadas, para que toda comparação seja contada. */
	private int comparar(K a, K b) {
		comparacoes++;
		return comparador.compare(a, b);
	}

	// ---------- consultas básicas ----------

	public boolean vazia() {
		return (raiz == null);
	}

	@Override
	public int tamanho() {
		return tamanho;
	}

	// ---------- inserção ----------

	/**
	 * Insere um item na árvore.
	 * @return a quantidade de itens da árvore após a inserção.
	 * @throws IllegalArgumentException se já existir um item com a mesma chave.
	 */
	@Override
	public int inserir(K chave, V item) {
		iniciarMedicao();
		try {
			raiz = inserirNo(raiz, chave, item);
			tamanho++;
			return tamanho;
		} finally {
			terminarMedicao();
		}
	}

	private No<K, V> inserirNo(No<K, V> atual, K chave, V item) {
		if (atual == null)
			return new No<>(chave, item);

		int resultado = comparar(chave, atual.getChave());
		if (resultado == 0)
			throw new IllegalArgumentException("O item já foi inserido anteriormente na árvore.");
		else if (resultado < 0)
			atual.setEsquerda(inserirNo(atual.getEsquerda(), chave, item));
		else
			atual.setDireita(inserirNo(atual.getDireita(), chave, item));

		return atual;
	}

	// ---------- pesquisa ----------

	/**
	 * Pesquisa um item pela chave.
	 * @return o item encontrado.
	 * @throws NoSuchElementException se a chave não estiver na árvore.
	 */
	@Override
	public V pesquisar(K chave) {
		iniciarMedicao();
		try {
			No<K, V> atual = raiz;
			while (atual != null) {
				int resultado = comparar(chave, atual.getChave());
				if (resultado == 0)
					return atual.getItem();
				atual = (resultado < 0) ? atual.getEsquerda() : atual.getDireita();
			}
			throw new NoSuchElementException("O item pesquisado não foi localizado na árvore!");
		} finally {
			terminarMedicao();
		}
	}

	// ---------- remoção ----------

	/**
	 * Remove o item que possui a chave informada.
	 * @return o item removido.
	 * @throws NoSuchElementException se a chave não estiver na árvore.
	 */
	@Override
	public V remover(K chave) {
		iniciarMedicao();
		try {
			itemRemovido = null;
			raiz = removerNo(raiz, chave);
			tamanho--;

			V removido = itemRemovido;
			itemRemovido = null;
			return removido;
		} finally {
			terminarMedicao();
		}
	}

	private No<K, V> removerNo(No<K, V> atual, K chave) {
		if (atual == null)
			throw new NoSuchElementException("O item a ser removido não foi localizado na árvore!");

		int resultado = comparar(chave, atual.getChave());
		if (resultado < 0)
			atual.setEsquerda(removerNo(atual.getEsquerda(), chave));
		else if (resultado > 0)
			atual.setDireita(removerNo(atual.getDireita(), chave));
		else {
			itemRemovido = atual.getItem();
			if (atual.getEsquerda() == null)        // 0 ou 1 filho (à direita)
				atual = atual.getDireita();
			else if (atual.getDireita() == null)    // 1 filho (à esquerda)
				atual = atual.getEsquerda();
			else                                    // 2 filhos: o maior da esquerda assume o lugar
				atual.setEsquerda(removerMaiorDaEsquerda(atual.getEsquerda(), atual));
		}
		return atual;
	}

	/** Remove o nó mais à direita da subárvore e copia seus dados para o nó que está sendo "removido". */
	private No<K, V> removerMaiorDaEsquerda(No<K, V> atual, No<K, V> destino) {
		if (atual.getDireita() != null) {
			atual.setDireita(removerMaiorDaEsquerda(atual.getDireita(), destino));
			return atual;
		}
		destino.setChave(atual.getChave());
		destino.setItem(atual.getItem());
		return atual.getEsquerda();
	}

	// ---------- recorte ----------

	/**
	 * Gera um "filtro" da árvore: uma lista, em ordem crescente de chave, com os itens cujas chaves
	 * estão entre chaveInicio e chaveFinal, inclusive. Subárvores que não podem conter chaves do
	 * intervalo não são visitadas. Se chaveInicio for maior que chaveFinal, a lista retorna vazia.
	 */
	public Lista<V> recortar(K chaveInicio, K chaveFinal) {
		Lista<V> recorte = new Lista<>();
		iniciarMedicao();
		recortarNo(raiz, chaveInicio, chaveFinal, recorte);
		terminarMedicao();
		return recorte;
	}

	private void recortarNo(No<K, V> atual, K chaveInicio, K chaveFinal, Lista<V> recorte) {
		if (atual == null)
			return;

		int emRelacaoAoInicio = comparar(chaveInicio, atual.getChave()); // <= 0: chave atual >= início
		int emRelacaoAoFinal = comparar(chaveFinal, atual.getChave());   // >= 0: chave atual <= final

		if (emRelacaoAoInicio < 0)    // ainda pode haver chaves do intervalo à esquerda
			recortarNo(atual.getEsquerda(), chaveInicio, chaveFinal, recorte);

		if (emRelacaoAoInicio <= 0 && emRelacaoAoFinal >= 0)
			recorte.inserir(atual.getItem());

		if (emRelacaoAoFinal > 0)     // ainda pode haver chaves do intervalo à direita
			recortarNo(atual.getDireita(), chaveInicio, chaveFinal, recorte);
	}

	// ---------- percurso ----------

	/** Percorre a árvore em ordem (crescente de chave), um item por linha. */
	@Override
	public String percorrer() {
		if (vazia())
			return "A árvore está vazia!";
		StringBuilder resultado = new StringBuilder();
		percorrerEmOrdem(raiz, resultado);
		return resultado.toString();
	}

	public String caminhamentoEmOrdem() {
		return percorrer();
	}

	private void percorrerEmOrdem(No<K, V> atual, StringBuilder resultado) {
		if (atual != null) {
			percorrerEmOrdem(atual.getEsquerda(), resultado);
			resultado.append(atual.getItem()).append("\n");
			percorrerEmOrdem(atual.getDireita(), resultado);
		}
	}

	@Override
	public String toString() {
		return percorrer();
	}
}