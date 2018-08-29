import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

class Token {
  public static final int ERROR = -1; // valor de erro

  String token; // token em si
  int end_tabela; // endereco na tabela
  int tamanho; // tamanho de uma constante
  int linha; // linha onde o token esta

  /**
   * Construtor padrao (usado para construcao de atributos).
   */
  public Token() {
    token = null;
    end_tabela = ERROR;
    tamanho = ERROR;
    linha = ERROR;
  }
}

class Analisador_Lexico {
  private static final int ERROR = -1; // valor de erro
  private static final int ESTADO_FINAL = 2; // estado final do automato

  private static int atual_token; // contagem de token atual
  private static boolean devolve; // boolean para devolver ultimo char
  public static ArrayList<Token> tokens; // registro unico dos tokens

  /**
   * Construtor padrao (usado para construcao de atributos).
   */
  public Analisador_Lexico() {
    atual_token = ERROR;
    tokens = new ArrayList<Token>();
  }

  /**
   * Metodo recebe um caracter e retorna se o mesmo e um digito
   * ou nao (numero)
   *
   * @param c caracter a ser testado
   * @return resultado do teste
   */
  private boolean isDigito(char c) {
    return c >= '0' && c <= '9';
  }

  /**
   * Metodo recebe um caracter e retorna se o mesmo e uma letra
   * ou nao, ignorando diferenca entre maiusculas ou minusculas
   *
   * @param c caracter a ser testado
   * @return resultado do teste
   */
  private boolean isLetra(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  /**
   * Metodo recebe um caracter e retorna se o mesmo e uma letra
   * que se encaixa no padrao Hexadecimal
   *
   * @param c caracter a ser testado
   * @return resultado do teste
   */
  private boolean isHexa(char c) {
    return (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  /**
   * Metodo recebe um caracter e retorna se o mesmo e um caractere
   * permitido na linguagem.
   *
   * @param c caracter a ser testado
   * @return resultado do teste
   */
  private boolean isPermitido(char c) {
    return isLetra(c) || isDigito(c) ||
           c == ' ' || c == '_' ||
           c == '.' || c == ',' ||
           c == ';' || c == '&' ||
           c == ':' || c == '(' ||
           c == ')' || c == '[' ||
           c == ']' || c == '{' ||
           c == '}' || c == '+' ||
           c == '-' || c == '\"' ||
           c == '\'' || c == '/' ||
           c == '%' || c == '^' ||
           c == '@' || c == '!' ||
           c == '?' || c == '>' ||
           c == '<' || c == '=' ||
           c == '\n' || c == '\r' || c == '*';
  }

  /**
   * Metodo recebe o nome do arquivo e a tabela de simbolos da linguagem
   * e percorre o arquivo equanto o analisa lexicamente para encontrar
   * possiveis erros.
   *
   * @param tabela tabela de simbolos padrao da linguagem
   * @param fileName nome do arquivo a ser lido (compilado)
   */
  public void analisarArquivo(Tabela_Simbolos tabela, String fileName) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      char c = ' ';
      devolve = false;
      String lexAtual = "";
      int linha_atual = 1;
      int estado_atual = 0;

      atual_token = 0; // iniciando

      // lendo character por character ate o fim do arquivo
      while(true) {
        // le o proximo caractere caso nao tenha um a ser avaliado (devolvido)
        if(!devolve) c = (char)reader.read();
        else devolve = false;

        // fim do arquivo
        if(c == (char)-1) break;

        // erro de caractere invalido
        if(!isPermitido(c) && estado_atual != 12 && estado_atual != 13) {
          System.out.printf("%d:caractere invalido.\n",
                            linha_atual);
          System.exit(0);
        }

        // estado de aceitacao
        if(estado_atual == ESTADO_FINAL) {
          estado_atual = 0;

          int pos = tabela.insertID(lexAtual);

          // Registro unico do token
          Token t = new Token();
          t.token = lexAtual;
          t.end_tabela = pos;
          t.tamanho = Token.ERROR;
          t.linha = linha_atual;

          tokens.add(t);
        }

        // voltando o automato
        if(estado_atual == 0) lexAtual = "";

        // ignorando carriage return (CR)
        if(c == '\r') continue;

        // erro de lexema nao identificado
        if(estado_atual == ERROR) {
          System.out.printf("%d:lexema nao identificado[%s].\n",
                            linha_atual,
                            lexAtual.replace("\n", "(ENTER)"));
          System.exit(0);
        }

        // contando as linhas
        if(c == '\n') linha_atual++;

        estado_atual = getTransicao(estado_atual, c);
        if(!devolve) lexAtual += c;
      }

      if(estado_atual != 0 && estado_atual != ESTADO_FINAL) {
        System.out.printf("%d:fim de arquivo nao esperado.\n",
                        linha_atual);
        System.exit(0);
      }
    }
    catch(Exception e) { e.printStackTrace(); }
  }

  /**
   * Metodo percorre o automato logico com valores entrados e retorna
   * o estado em que o mesmo para ao receber tais entradas.
   * <p>
   * Caso a transicao seja invalida ou a entrada nao esteja correta
   * retorna um codigo de erro.
   *
   * @param from endereco do estado atual
   * @param to valor da string da transicao a ser testada
   * @param devolve booleano que indica se deve devolver o caracter
   * @return endereco do estado final da transicao
   */
  private int getTransicao(int from, char to) {
    switch(from){ // from = numero do estado
      case 0:
        if(to == ' ' || to == '\n') return 0;
        if(isLetra(to)) return 1;
        if(to == ',' ||
           to == ';' ||
           to == '(' ||
           to == ')' ||
           to == '+' ||
           to == '-' ||
           to == '*' ||
           to == '{' ||
           to == '}' ||
           to == '[' ||
           to == ']' ||
           to == '%' ||
           to == '=' ||
           to == '$') return 2;
        if(to == '.' || to == '_') return 3;
        if(to == '0') return 4;
        if(to == '1') return 6;
        if(isDigito(to)) return 7;
        if(to == '\"') return 8;
        if(to == '<') return 9;
        if(to == '>') return 10;
        if(to == '/') return 11;
        if(to == '\'') return 14;
        return ERROR;
      case 1:
        if(isLetra(to) ||
           isDigito(to) ||
           to == '.' ||
           to == '_') return 1;
        devolve = true;
        return 2;
      case 2:
        return ERROR;
      case 3:
        if(isLetra(to) ||
           isDigito(to)) return 1;
        if(to == '.' ||
           to == '_') return 3;

        return ERROR;
      case 4:
        if(isDigito(to)) return 7;
        if(to == 'x') return 5;
        devolve = true;
        return 2;
      case 5:
        if(isDigito(to) ||
           isHexa(to)) return 51;
        return ERROR;
      case 51: // estado extra para ajuste do automato
        if(isDigito(to) ||
           isHexa(to)) return 2;
        return ERROR;
      case 6:
        if(isDigito(to)) return 7;
        devolve = true;
        return 2;
      case 7:
        if(isDigito(to)) return 7;
        devolve = true;
        return 2;
      case 8:
        if(to == '\"') return 2;
        if(to == '$' ||
           to == '\"' ||
           to == '\n') return ERROR;
        return 8;
      case 9:
        if(to == '>' ||
           to == '=') return 2;
        devolve = true;
        return 2;
      case 10:
        if(to == '=') return 2;
        devolve = true;
        return 2;
      case 11:
        if(to == '*') return 12;
        devolve = true;
        return 2;
      case 12:
        if(to == '*') return 13;
        return 12;
      case 13:
        if(to == '*') return 13;
        if(to == '/') return 0;
        return 12;
      case 14:
        if(isPermitido(to)) return 15;
        return ERROR;
      case 15:
        if(to == '\'') return 2;
        return ERROR;
    }

    return ERROR;
  }

  /**
   * Metodo para receber o proximo token no registro de tokens salvo.
   * <p>
   * Metodo retorna um objeto Token contendo as informacoes do token
   * caso ainda haja tokens a serem lidos e um token vazio caso o
   * registro de tokens tenha chegado ao fim.
   *
   * @return proximo token na sequencia
   */
  public Token nextToken() {
    if(atual_token < tokens.size()) return tokens.get(atual_token++);
    Token aux = new Token();
    aux.token = "EOF";
    return aux;
  }
}
