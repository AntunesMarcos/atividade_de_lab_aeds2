public class No<K, V> {

	private K chave;       // chave identificadora do item armazenado no nodo da árvore.
	private V item;        // contém os dados do item armazenado no nodo da árvore.
	private No<K, V> direita;    // referência ao nodo armazenado, na árvore, à direita do nó em questão.
	private No<K, V> esquerda;   // referência ao nodo armazenado, na árvore, à esquerda do nó em questão.
	
	public No(K chave, V item) {
		setChave(chave);
		setItem(item);
	    setDireita(null);
	    setEsquerda(null);
	}

	public V getItem() {
		return item;
	}

	public void setItem(V item) {
		this.item = item;
	}

	public K getChave() {
		return chave;
	}

	public void setChave(K chave) {
		this.chave = chave;
	}
	
	public No<K, V> getDireita() {
		return direita;
	}

	public void setDireita(No<K, V> direita) {
		this.direita = direita;
	}

	public No<K, V> getEsquerda() {
		return esquerda;
	}

	public void setEsquerda(No<K, V> esquerda) {
		this.esquerda = esquerda;
	}
}