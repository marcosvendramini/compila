import java.util.ArrayList;

class Simbolo {
  String simbolo; // simbolo
  int tamanho; // tamanho do id se for um vetor

  // classe do simbolo onde:
  //  0 - classe-var
  //  1 - classe-const
  // -1 - sem valor
  int classe;

  // classe do simbolo onde:
  //  0 - inteiro
  //  1 - caractere
  // -1 - sem valor
  int tipo;

  /**
   * Construtor padrao (usado para construcao de atributos).
   */
  public Simbolo(String simbolo) {
    this.simbolo = simbolo;
    this.classe = -1;
    this.tipo = -1;
    this.tamanho = 0;
  }
}

class Tabela_Simbolos {
  private static final int ERROR = -1; // valor de erro
  private ArrayList<Simbolo> tabela_de_simbolos; // pesquisas bidirecionais
  private static final int ID_INIT = 33; // posicao onde comecam os ids

 /**
  * Construtor padrao (usado para construcao de atributos).
  */
  public Tabela_Simbolos() {
    initTabela();
  }

  /**
   * Metodo cria a tabela de simbolos e insere os lexemas
   * que a mesma deve possuir.
   */
  private void initTabela() {
    tabela_de_simbolos = new ArrayList<Simbolo> () {{
      add(new Simbolo("const")); add(new Simbolo("var")); add(new Simbolo("integer"));
      add(new Simbolo("char")); add(new Simbolo("for")); add(new Simbolo("if"));
      add(new Simbolo("else")); add(new Simbolo("and")); add(new Simbolo("or"));
      add(new Simbolo("not")); add(new Simbolo("=")); add(new Simbolo("to"));
      add(new Simbolo("(")); add(new Simbolo(")")); add(new Simbolo(">"));
      add(new Simbolo("<")); add(new Simbolo("<>")); add(new Simbolo(">="));
      add(new Simbolo("<=")); add(new Simbolo(",")); add(new Simbolo("+"));
      add(new Simbolo("-")); add(new Simbolo("*")); add(new Simbolo("/"));
      add(new Simbolo(";")); add(new Simbolo("{")); add(new Simbolo("}"));
      add(new Simbolo("then")); add(new Simbolo("readln")); add(new Simbolo("step"));
      add(new Simbolo("write")); add(new Simbolo("writeln")); add(new Simbolo("%"));
      add(new Simbolo("]")); add(new Simbolo("[")); add(new Simbolo("do"));
    }};
  }

  /**
   * Metodo testa se o identificador vindo por parametro e um ID ou se
   * o mesmo e uma palavra reservada.
   *
   * @param ID identificador a ser testado
   * @return true caso a entrada seja um ID, false caso contrario
   */
  public boolean isID(String ID) {
    int find = findByID(ID);

    if(find != ERROR) {
      if(find >= ID_INIT) return true;
      return false;
    }

    return false;
  }

  /**
    * Metodo retorna qual objeto esta contido na posicao pesquisada.
    * <p>
    * Caso a posicao esteja na extensao da tabela retorna sua posicao,
    * caso contrario retorna nulo.
    *
    * @param pos posicao do simbolo a ser localizado
    * @return objeto Simbolo contido na posicao pesquisada
    */
  public Simbolo getID(int pos) {
    if(pos >= 0 && pos < tabela_de_simbolos.size()) {
      return tabela_de_simbolos.get(pos);
    }

    return null;
  }

  /**
   * Metodo tenta inserir o identificador na tabela de simbolos e retorna
   * a posicao em que o mesmo foi inserido caso bem sucedido.
   * <p>
   * Caso o elemento ja exista na tabela o metodo retorna um codigo de erro.
   *
   * @param ID identificador a ser inserido na tabela
   * @return posicao do elemento inserido na tabela
   */
  public int insertID(String ID) {
    int find = findByID(ID);

    if(find == ERROR) {
      tabela_de_simbolos.add(new Simbolo(ID));
      return tabela_de_simbolos.size()-1;
    }

    return find;
  }

  /**
   * Metodo pesquisa o identificador na tabela e retorna a posicao
   * do identificador caso seja encontrado.
   * <p>
   * Caso contrario retorna codigo de erro.
   *
   * @param ID identificador a ser pesquisado
   * @return posicao do elemento inserido na tabela
   */
  public int findByID(String ID) {
    for(int i = 0; i < tabela_de_simbolos.size(); i++)
      if(tabela_de_simbolos.get(i).simbolo.equalsIgnoreCase(ID))
        return i;

    return ERROR;
  }

  /**
   * Metodo imprime na tela toda a tabela de simbolos linha a linha
   */
  public void printTokens() {
      for(Simbolo s : tabela_de_simbolos)
        System.out.println(s.simbolo);
  }
}
