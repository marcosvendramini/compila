import java.io.*;
import java.util.ArrayList;

class Analisador_Sintatico {
	public static Token token_corrente                  = new Token();
	public static Tabela_Simbolos tabela_simbolos       = new Tabela_Simbolos();
	private static Analisador_Lexico Analisador_Lexico  = new Analisador_Lexico();
  private static int last_line_read                   = 0;
  private static final int ERROR                      = -1;
	private static final int TIPO_INTEIRO               = 0;
	private static final int TIPO_CHAR                  = 1;
	private static final int CLASSE_VAR                 = 0;
	private static final int CLASSE_CONST               = 1;

	private static BufferedWriter arq8086;
	private static int END_IN_MEM 											= 4000;


	public static void main(String []args) {
		init_sintatico(args[0], args[1]);
	}

  /**
   * Metodo inicializa o analisador sintatico e comeca a execucao do
   * procedimento inicial.
   *
   * @param nome_arquivo nome do arquivo a ser analisado
   */
	public static void init_sintatico(String nome_arquivo, String arquivo_masm) {
		try {
			if(isL(nome_arquivo)) {
				Analisador_Lexico.analisarArquivo(tabela_simbolos,nome_arquivo);

				// Recebe primeiro token
				token_corrente = Analisador_Lexico.nextToken();
				arq8086 = new BufferedWriter(new FileWriter(arquivo_masm));

				S();
			}

		} catch(Exception e) { e.printStackTrace(); }
	}

  /**
   * Metodo testa se o nome do arquivo recebido e de extensao (.l), que e
   * o tipo de arquivo a ser compilado.
   *
   * @param arg nome do arquivo a ser testado
   * @return resultado do teste
   */
	public static boolean isL(String arg) {
		return arg.substring(arg.lastIndexOf('.')).equalsIgnoreCase(".l");
	}

  /**
   * Metodo checka se o token esperado se iguala ao token recebido
   * pelo analisador lexico. Caso seja diferente, imprime uma mensagem
   * de erro e encerra o programa.
   *
   * @param token_esperado token a ser comparado
   */
	private static void CasaToken(String token_esperado) {

		if(token_corrente.token.trim().equalsIgnoreCase(token_esperado.trim())) {
			last_line_read = token_corrente.linha;
			token_corrente = Analisador_Lexico.nextToken();
		} else {
      if(token_corrente.token.trim().equalsIgnoreCase("EOF")) {
        System.out.printf("%d:fim de arquivo nao esperado.\n",
                        last_line_read);
      } else {
        System.out.println(token_corrente.linha+":token nao esperado ["+token_corrente.token+"].");
      }
			System.exit(0);
		}
	}

  /**
   * Metodo checka se o token de escopo global e um ID ao checkar se o mesmo
   * esta inserido na tabela de simbolos. Caso nao seja, imprime uma mensagem
   * de erro e encerra o programa.
   */
	private static void CasaTokenId() {
		if(tabela_simbolos.isID(token_corrente.token.trim())) {
			last_line_read = token_corrente.linha;
			token_corrente = Analisador_Lexico.nextToken();
		} else {
      if(token_corrente.token.trim().equalsIgnoreCase("EOF")) {
        System.out.printf("%d:fim de arquivo nao esperado.\n",
                        last_line_read);
      } else {
        System.out.println(token_corrente.linha+":token nao esperado ["+token_corrente.token+"].");
      }
			System.exit(0);
		}
	}

  /**
   * Metodo checka se o token de escopo global e uma constante.
   * <p>
   * Metodo ainda nao implementado.

	 * @return tipo da constante
   */
	private static int CasaTokenConstante() {
		Token t = token_corrente;
		int tipo = -1;

		if(t.token.startsWith("0x") || t.token.startsWith("0X"))
			tipo = TIPO_INTEIRO;
		if(t.token.charAt(0) >= '0' && t.token.charAt(0) <= '9')
			tipo = TIPO_INTEIRO;
		if(t.token.charAt(0) == '\'')
			tipo = TIPO_CHAR;
		if(t.token.charAt(0) == '\"')
			tipo = TIPO_CHAR;


		if(tipo != ERROR) CasaTokenId();

		return tipo;
	}

  /**
   * Metodo que implementa o procedimento S como especificado pela gramatica.
   */
	private static void S() throws IOException {
    if(!token_corrente.token.equalsIgnoreCase("EOF")) {

			arq8086.write("sseg SEGMENT STACK");
			arq8086.newLine();
			arq8086.write("byte 4000h DUP(?)");
			arq8086.newLine();
			arq8086.write("sseg ENDS");
			arq8086.newLine();
			arq8086.write("dseg SEGMENT PUBLIC");
			arq8086.newLine();
			arq8086.write("byte 4000h DUP(?)");
			arq8086.newLine();

    	if(token_corrente.token.equalsIgnoreCase("var")) {
    		D();
    	}

      if(token_corrente.token.equalsIgnoreCase("const")) {
    		CO();
    	}

			arq8086.write("dseg ENDS");
			arq8086.newLine();
			arq8086.write("csed SEGMENT PUBLIC");
			arq8086.newLine();
			arq8086.write("ASSUME CS:cseg, DS:dseg");
			arq8086.newLine();
			arq8086.write("strt");
			arq8086.newLine();

      do{
        if(token_corrente.token.equalsIgnoreCase("for")) {
      		R();
      	} else if(token_corrente.token.equalsIgnoreCase("if")) {
      		TES();
      	} else if(token_corrente.token.equalsIgnoreCase(";")) {
      		NU();
      	} else if(token_corrente.token.equalsIgnoreCase("readln")) {
      		L();
      	} else if(token_corrente.token.equalsIgnoreCase("write") ||
                 token_corrente.token.equalsIgnoreCase("writeln")) {

          E();
      	}
        else if(tabela_simbolos.isID(token_corrente.token)) {
      		AT();
        } else {
          break;
        }
      } while(true);

			arq8086.write("cseg ENDS");
			arq8086.newLine();
			arq8086.write("END strt");
			arq8086.newLine();
    }

		arq8086.flush();
		arq8086.close();
	}

  /**
   * Metodo que implementa o procedimento D como especificado pela gramatica.
   */
	private static void D() throws IOException {
		CasaToken("var");
		A();
	}

  /**
   * Metodo que implementa o procedimento A como especificado pela gramatica.
   */
	private static void A() throws IOException {
    do {
  		if(token_corrente.token.equalsIgnoreCase("integer")) {
  			CasaToken("integer");
  			Z();
        CasaToken(";");
  		} else{
  			CasaToken("char");
  			W();
        CasaToken(";");
  		}
    } while(token_corrente.token.equalsIgnoreCase("integer") ||
            token_corrente.token.equalsIgnoreCase("char"));
	}

  /**
   * Metodo que implementa o procedimento Z como especificado pela gramatica.
	 *
	 * @return
	 */
	private static int Z() throws IOException {
		int tipo = -1;

		if(tabela_simbolos.getID(token_corrente.end_tabela).tipo == ERROR) {
			tabela_simbolos.getID(token_corrente.end_tabela).tipo = TIPO_INTEIRO;
		}
		else {
			System.out.printf("%d:identificador ja declarado [%s].\n",
											last_line_read, token_corrente.token);
			System.exit(0);
		}

		tabela_simbolos.getID(token_corrente.end_tabela).classe = CLASSE_VAR;


		tipo = I();
		K(tipo);

		arq8086.write("sword ?");
		arq8086.newLine();

		return tipo;
	}

  /**
   * Metodo que implementa o procedimento W como especificado pela gramatica.
	 *
	 * @return
   */
	private static int W() throws IOException {
		int tipo = -1;

		if(tabela_simbolos.getID(token_corrente.end_tabela).tipo == ERROR) {
			tabela_simbolos.getID(token_corrente.end_tabela).tipo = TIPO_CHAR;
		}
		else {
			System.out.printf("%d:identificador ja declarado [%s].\n",
											last_line_read, token_corrente.token);
			System.exit(0);
		}

		tipo = I();
		C(tipo);

		arq8086.write("byte ?");
		arq8086.newLine();

		return tipo;
	}

  /**
   * Metodo que implementa o procedimento K como especificado pela gramatica.
	 *
	 * @param tipo
   */
	private static void K(int tipo) throws IOException {
		if(token_corrente.token.equals(",")) {
			CasaToken(",");

			int z = Z();
			if(z != ERROR && z != tipo){
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}

		} else if(token_corrente.token.equals("=")) {
			CasaToken("=");

			if(token_corrente.token.equals("-")) {
				CasaToken("-");
			}

			String cons = token_corrente.token;

			if(CasaTokenConstante() != tipo) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}

			if(token_corrente.token.equals(",")) {
				CasaToken(",");
				Z();
			}

			arq8086.write("mov DS:[" + END_IN_MEM + "], " + cons);
			arq8086.newLine();
		}
	}

  /**
   * Metodo que implementa o procedimento C como especificado pela gramatica.
	 *
	 * @param tipo
   */
	private static void C(int tipo) throws IOException {
		if(token_corrente.token.equals(",")) {
			CasaToken(",");

			int w = W();
			if(w != ERROR && w != tipo) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}

		} else if(token_corrente.token.equals("=")) {
			CasaToken("=");

			String cons = token_corrente.token;

			if(CasaTokenConstante() != tipo) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}

			if(token_corrente.token.equals(",")) {
				CasaToken(",");
				W();
			}

			arq8086.write("mov DS:[" + END_IN_MEM + "], " + cons);
			arq8086.newLine();
		}
	}

  /**
   * Metodo que implementa o procedimento CO como especificado pela gramatica.
   */
	private static void CO() throws IOException {
		CasaToken("const");
		X();
    CasaToken(";");
	}

  /**
   * Metodo que implementa o procedimento X como especificado pela gramatica.
   */
	private static void X() throws IOException {
    Y();
    while(token_corrente.token.equals(",")) {
      CasaToken(",");
      Y();
    }
	}

  /**
   * Metodo que implementa o procedimento Y como especificado pela gramatica.
   */
  private static void Y() throws IOException {
		Token t = token_corrente;
		tabela_simbolos.getID(t.end_tabela).tipo = -2;

    I();
    CasaToken("=");
    if(token_corrente.token.equals("-")) {
      CasaToken("-");
    }

		String cons = token_corrente.token;
		int tipo = CasaTokenConstante();
		int tt = tabela_simbolos.getID(t.end_tabela).tipo;
		if(tt == ERROR || tt == -2) {
		 	tabela_simbolos.getID(t.end_tabela).tipo = tipo;
		}
		else {
			System.out.printf("%d:identificador ja declarado [%s].\n",
											last_line_read, token_corrente.token);
			System.exit(0);
		}

		if(tipo == TIPO_INTEIRO) {
			arq8086.write("sword ?");
			arq8086.newLine();
		}
		else {
			arq8086.write("byte ?");
			arq8086.newLine();
		}

		arq8086.write("mov DS:[" + END_IN_MEM + "], " + cons);
		arq8086.newLine();

		tabela_simbolos.getID(t.end_tabela).classe = CLASSE_CONST;
  }

  /**
   * Metodo que implementa o procedimento AT como especificado pela gramatica.
   */
	private static void AT() throws IOException {
		int tipo = -1;

		if(tabela_simbolos.getID(token_corrente.end_tabela).classe != CLASSE_VAR){
			System.out.printf("%d:classe de identificador incompativel[%s].\n",
											last_line_read, token_corrente.token);
			System.exit(0);
		}

		tipo = I();
		CasaToken("=");
		if(EXP() != tipo) {
			System.out.printf("%d:classe de identificador incompativel[%s].\n",
											last_line_read, token_corrente.token);
			System.exit(0);
		}

		arq8086.write("mov DS:[" + END_IN_MEM + "], " + 0); // EXP VALUE
		arq8086.newLine();
	}

  /**
   * Metodo que implementa o procedimento R como especificado pela gramatica.
   */
	private static void R() throws IOException {
		int tipo = -1;

		CasaToken("for");

		if(tabela_simbolos.getID(token_corrente.end_tabela).classe != CLASSE_VAR){
			System.out.printf("%d:classe de identificador incompativel[%s].\n",
											last_line_read, token_corrente.token);
			System.exit(0);
		}

		tipo = I();
		CasaToken("=");

		int e = EXP();
		if(e != ERROR && e != tipo) {
			System.out.printf("%d:tipos incompativeis.\n", last_line_read);
			System.exit(0);
		}

		CasaToken("to");

		e = EXP();
		if(e != ERROR && e != tipo) {
			System.out.printf("%d:tipos incompativeis.\n", last_line_read);
			System.exit(0);
		}

		arq8086.write("mov DS:[" + END_IN_MEM + "], " + 0); // EXP value
		arq8086.newLine();
		arq8086.write("mov Ax, " + 0); // EXP2 value
		arq8086.newLine();
		arq8086.write("iniFor:");
		arq8086.newLine();
		arq8086.write("mov BX, DS:[" + END_IN_MEM + "]");
		arq8086.newLine();
		arq8086.write("cmp ax, bx");
		arq8086.newLine();
		arq8086.write("je fimFor");
		arq8086.newLine();

		if(token_corrente.token.trim().equalsIgnoreCase("step")) {
			CasaToken("step");

			String step = token_corrente.token;
			CasaTokenConstante();

			arq8086.write("add bx, " + step);
			arq8086.newLine();
			arq8086.write("mov DS:[" + END_IN_MEM + "], bx");
			arq8086.newLine();
		}

		CasaToken("do");
		P();

		arq8086.write("fimFor:");
		arq8086.newLine();
	}

  /**
   * Metodo que implementa o procedimento P como especificado pela gramatica.
   */
	private static void P() throws IOException {
		if(token_corrente.token.equals("{")) {
			CasaToken("{");
			S();
			CasaToken("}");
		} else{
			S();
		}
	}

  /**
   * Metodo que implementa o procedimento TES como especificado pela gramatica.
	 *
	 * @return
   */
	private static int TES() throws IOException {
		int tipo = -1;
		CasaToken("if");
		tipo = EXP();

		arq8086.write("mov bx, EXPval");
		arq8086.newLine();
		arq8086.write("Cmp bx, 1");
		arq8086.newLine();

		CasaToken("then");

		arq8086.write("jne rotFalso");
		arq8086.newLine();
		arq8086.write("jmp rotFim");
		arq8086.newLine();

		P();
		if(token_corrente.token.equals("else")) {
			CasaToken("else");
			P();

			arq8086.write("rotFalso: ");
			arq8086.newLine();
		}

		arq8086.write("rotFim:");
		arq8086.newLine();

		return tipo;
	}

  /**
   * Metodo que implementa o procedimento NU como especificado pela gramatica.
   */
	private static void NU() throws IOException {
		CasaToken(";");
	}

  /**
   * Metodo que implementa o procedimento L como especificado pela gramatica.
   */
	private static void L() throws IOException {
		CasaToken("readln");
		CasaToken("(");
		I();
		CasaToken(")");
		CasaToken(";");

		arq8086.write("mov  dx, buffer.end");
		arq8086.newLine();
		arq8086.write("mov  al, 0FFh");
		arq8086.newLine();
		arq8086.write("mov  ds:[buffer.end], al");
		arq8086.newLine();
		arq8086.write("mov  ah, 0Ah");
		arq8086.newLine();
		arq8086.write("int  21h");
		arq8086.newLine();
		arq8086.write("mov  di, buffer.end+2 ;posição do string");
		arq8086.newLine();
		arq8086.write("mov  ax, 0   ;acumulador");
		arq8086.newLine();
		arq8086.write("mov  cx, 10   ;base decimal");
		arq8086.newLine();
		arq8086.write("mov  dx, 1   ;valor sinal +");
		arq8086.newLine();
		arq8086.write("mov  bh, 0");
		arq8086.newLine();
		arq8086.write("mov  bl, ds:[di]  ;caractere");
		arq8086.newLine();
		arq8086.write("cmp  bx, 2Dh   ;verifica sinal");
		arq8086.newLine();
		arq8086.write("jne  R0    ;se não negativo");
		arq8086.newLine();
		arq8086.write("mov  dx, -1   ;valor sinal -");
		arq8086.newLine();
		arq8086.write("add  di, 1   ;incrementa base");
		arq8086.newLine();
		arq8086.write("mov  bl, ds:[di]  ;próximo caractere");
		arq8086.newLine();
		arq8086.write("R0:");
		arq8086.newLine();
		arq8086.write("push dx    ;empilha sinal");
		arq8086.newLine();
		arq8086.write("mov  dx, 0   ;reg. multiplicação");
		arq8086.newLine();
		arq8086.write("R1:");
		arq8086.newLine();
		arq8086.write("cmp  bx, 0dh   ;verifica fim string");
		arq8086.newLine();
		arq8086.write("je  R2    ;salta se fim string");
		arq8086.newLine();
		arq8086.write("imul cx    ;mult. 10");
		arq8086.newLine();
		arq8086.write("add  bx, -48   ;converte caractere");
		arq8086.newLine();
		arq8086.write("add  ax, bx   ;soma valor caractere");
		arq8086.newLine();
		arq8086.write("add  di, 1   ;incrementa base");
		arq8086.newLine();
		arq8086.write("mov  bh, 0");
		arq8086.newLine();
		arq8086.write("mov  bl, ds:[di]  ;próximo caractere");
		arq8086.newLine();
		arq8086.write("jmp  R1    ;loop");
		arq8086.newLine();
		arq8086.write("R2:");
		arq8086.newLine();
		arq8086.write("pop  cx    ;desempilha sinal");
		arq8086.newLine();
		arq8086.write("imul cx    ;mult. sinal");
		arq8086.newLine();
		arq8086.write("mov DS:[id_end], AX");
		arq8086.write("else");
		arq8086.newLine();
		arq8086.write("mov  di, buffer.end+2");
		arq8086.newLine();
		arq8086.write("mov  bl, ds:[di]");
		arq8086.newLine();
		arq8086.write("mov DS:[id_end], bl");
		arq8086.newLine();
	}

  /**
   * Metodo que implementa o procedimento E como especificado pela gramatica.
   */
	private static void E() throws IOException {
		if(token_corrente.token.equals("write")) {
			CasaToken("write");
			CasaToken("(");
			EXP();

			while(token_corrente.token.equals(",")) {
				CasaToken(",");
				EXP();
			}

			CasaToken(")");
			CasaToken(";");

			arq8086.write("mov  di, string.end ;end. string temp.");
			arq8086.newLine();
			arq8086.write("mov  cx, 0  ;contador");
			arq8086.newLine();
			arq8086.write("cmp  ax,0  ;verifica sinal");
			arq8086.newLine();
			arq8086.write("jge  R0   ;salta se número positivo");
			arq8086.newLine();
			arq8086.write("mov  bl, 2Dh  ;senão, escreve sinal –");
			arq8086.newLine();
			arq8086.write("mov  ds:[di], bl");
			arq8086.newLine();
			arq8086.write("add  di, 1  ;incrementa índice");
			arq8086.newLine();
			arq8086.write("neg  ax   ;toma módulo do número");
			arq8086.newLine();
			arq8086.write("R0:");
			arq8086.newLine();
			arq8086.write("mov  bx, 10  ;divisor");
			arq8086.newLine();
			arq8086.write("R1:");
			arq8086.newLine();
			arq8086.write("add  cx, 1  ;incrementa contador");
			arq8086.newLine();
			arq8086.write("mov  dx, 0  ;estende 32bits p/ div.");
			arq8086.newLine();
			arq8086.write("idiv bx     ;divide DXAX por BX");
			arq8086.newLine();
			arq8086.write("push  dx   ;empilha valor do resto");
			arq8086.newLine();
			arq8086.write("cmp  ax, 0  ;verifica se quoc. é 0");
			arq8086.newLine();
			arq8086.write("jne  R1   ;se não é 0, continua");
			arq8086.newLine();
			arq8086.write(";agora, desemp. os valores e escreve o string");
			arq8086.newLine();
			arq8086.write("R2:");
			arq8086.newLine();
			arq8086.write("pop  dx   ;desempilha valor");
			arq8086.newLine();
			arq8086.write("add  dx, 30h  ;transforma em caractere");
			arq8086.newLine();
			arq8086.write("mov  ds:[di],dl ;escreve caractere");
			arq8086.newLine();
			arq8086.write("add  di, 1  ;incrementa base");
			arq8086.newLine();
			arq8086.write("add  cx, -1  ;decrementa contador");
			arq8086.newLine();
			arq8086.write("cmp  cx, 0  ;verifica pilha vazia");
			arq8086.newLine();
			arq8086.write("jne  R2   ;se não pilha vazia, loop");
			arq8086.newLine();
			arq8086.write(";grava fim de string");
			arq8086.newLine();
			arq8086.write("mov  dl, 024h  ;fim de string");
			arq8086.newLine();
			arq8086.write("mov  ds:[di], dl  ;grava '$'");
			arq8086.newLine();
			arq8086.write(";exibe string");
			arq8086.newLine();
			arq8086.write("mov  dx, string.end");
			arq8086.newLine();
			arq8086.write("mov  ah, 09h");
			arq8086.newLine();
			arq8086.write("int  21h");
			arq8086.newLine();

		} else{
			CasaToken("writeln");
			CasaToken("(");
			EXP();

			while(token_corrente.token.equals(",")) {
				CasaToken(",");
				EXP();
			}

			CasaToken(")");
			CasaToken(";");

			arq8086.write("mov  di, string.end ;end. string temp.");
			arq8086.newLine();
			arq8086.write("mov  cx, 0  ;contador");
			arq8086.newLine();
			arq8086.write("cmp  ax,0  ;verifica sinal");
			arq8086.newLine();
			arq8086.write("jge  R0   ;salta se número positivo");
			arq8086.newLine();
			arq8086.write("mov  bl, 2Dh  ;senão, escreve sinal –");
			arq8086.newLine();
			arq8086.write("mov  ds:[di], bl");
			arq8086.newLine();
			arq8086.write("add  di, 1  ;incrementa índice");
			arq8086.newLine();
			arq8086.write("neg  ax   ;toma módulo do número");
			arq8086.newLine();
			arq8086.write("R0:");
			arq8086.newLine();
			arq8086.write("mov  bx, 10  ;divisor");
			arq8086.newLine();
			arq8086.write("R1:");
			arq8086.newLine();
			arq8086.write("add  cx, 1  ;incrementa contador");
			arq8086.newLine();
			arq8086.write("mov  dx, 0  ;estende 32bits p/ div.");
			arq8086.newLine();
			arq8086.write("idiv bx     ;divide DXAX por BX");
			arq8086.newLine();
			arq8086.write("push  dx   ;empilha valor do resto");
			arq8086.newLine();
			arq8086.write("cmp  ax, 0  ;verifica se quoc. é 0");
			arq8086.newLine();
			arq8086.write("jne  R1   ;se não é 0, continua");
			arq8086.newLine();
			arq8086.write(";agora, desemp. os valores e escreve o string");
			arq8086.newLine();
			arq8086.write("R2:");
			arq8086.newLine();
			arq8086.write("pop  dx   ;desempilha valor");
			arq8086.newLine();
			arq8086.write("add  dx, 30h  ;transforma em caractere");
			arq8086.newLine();
			arq8086.write("mov  ds:[di],dl ;escreve caractere");
			arq8086.newLine();
			arq8086.write("add  di, 1  ;incrementa base");
			arq8086.newLine();
			arq8086.write("add  cx, -1  ;decrementa contador");
			arq8086.newLine();
			arq8086.write("cmp  cx, 0  ;verifica pilha vazia");
			arq8086.newLine();
			arq8086.write("jne  R2   ;se não pilha vazia, loop");
			arq8086.newLine();
			arq8086.write(";grava fim de string");
			arq8086.newLine();
			arq8086.write("mov  dl, 024h  ;fim de string");
			arq8086.newLine();
			arq8086.write("mov  ds:[di], dl  ;grava '$'");
			arq8086.newLine();
			arq8086.write(";exibe string");
			arq8086.newLine();
			arq8086.write("mov  dx, string.end");
			arq8086.newLine();
			arq8086.write("mov  ah, 09h");
			arq8086.newLine();
			arq8086.write("int  21h");
			arq8086.newLine();
			arq8086.write("mov  ah, 02h");
			arq8086.newLine();
			arq8086.write("mov  dl, 0Dh");
			arq8086.newLine();
			arq8086.write("int  21h");
			arq8086.newLine();
			arq8086.write("mov  DL, 0Ah");
			arq8086.newLine();
			arq8086.write("int  21h");
			arq8086.newLine();
		}
	}

  /**
   * Metodo que implementa o procedimento EXP como especificado pela gramatica.
	 *
	 * @return retorna o tipo do procedimento
   */
	private static int EXP() throws IOException {
		int tipo = -1;
		int tipo2 = -1;
		boolean no_Char_op = false; // operacao nao valida para char
		String exps = "nada";

		tipo = EXPS();
		if(token_corrente.token.equals("=")) {
			CasaToken("=");
			exps = token_corrente.token;
			tipo2 = EXPS();

			arq8086.write("Mov  ax, ds:[exps1.end]");
			arq8086.newLine();
			arq8086.write("Mov  bx, ds:[exps2.end]");
			arq8086.newLine();
			arq8086.write("Cmp ax,bx");
			arq8086.newLine();
			arq8086.write("Je truexp");
			arq8086.newLine();
			arq8086.write("Mov ax,0;");
			arq8086.newLine();
			arq8086.write("Jmp fimexp");
			arq8086.newLine();
			arq8086.write("Truexp:");
			arq8086.newLine();
			arq8086.write("Mov ax,1;");
			arq8086.newLine();
			arq8086.write("Fimexp:");
			arq8086.newLine();
			arq8086.write("Mov ds:[exp.end], ax");
			arq8086.newLine();

		} else if(token_corrente.token.equals("<>")) {
			CasaToken("<>");
			exps = token_corrente.token;
			tipo2 = EXPS();

			arq8086.write("Mov  ax, ds:[exps1.end]");
			arq8086.newLine();
			arq8086.write("Mov  bx, ds:[exps2.end]");
			arq8086.newLine();
			arq8086.write("Cmp ax,bx");
			arq8086.newLine();
			arq8086.write("Jne truexp");
			arq8086.newLine();
			arq8086.write("Mov ax,0;");
			arq8086.newLine();
			arq8086.write("Jmp fimexp");
			arq8086.newLine();
			arq8086.write("Truexp:");
			arq8086.newLine();
			arq8086.write("Mov ax,1;");
			arq8086.newLine();
			arq8086.write("Fimexp:");
			arq8086.newLine();
			arq8086.write("Mov ds:[exp.end], ax");
			arq8086.newLine();

		} else if(token_corrente.token.equals("<")) {
			CasaToken("<");
			exps = token_corrente.token;
			tipo2 = EXPS();
			no_Char_op = true;

			arq8086.write("Mov  ax, ds:[exps1.end]");
			arq8086.newLine();
			arq8086.write("Mov  bx, ds:[exps2.end]");
			arq8086.newLine();
			arq8086.write("Cmp ax,bx");
			arq8086.newLine();
			arq8086.write("Jl truexp");
			arq8086.newLine();
			arq8086.write("Mov ax,0;");
			arq8086.newLine();
			arq8086.write("Jmp fimexp");
			arq8086.newLine();
			arq8086.write("Truexp:");
			arq8086.newLine();
			arq8086.write("Mov ax,1;");
			arq8086.newLine();
			arq8086.write("Fimexp:");
			arq8086.newLine();
			arq8086.write("Mov ds:[exp.end], ax");
			arq8086.newLine();

		} else if(token_corrente.token.equals(">")) {
			CasaToken(">");
			exps = token_corrente.token;
			tipo2 = EXPS();
			no_Char_op = true;

			arq8086.write("Mov  ax, ds:[exps1.end]");
			arq8086.newLine();
			arq8086.write("Mov  bx, ds:[exps2.end]");
			arq8086.newLine();
			arq8086.write("Cmp ax,bx");
			arq8086.newLine();
			arq8086.write("Jg truexp");
			arq8086.newLine();
			arq8086.write("Mov ax,0;");
			arq8086.newLine();
			arq8086.write("Jmp fimexp");
			arq8086.newLine();
			arq8086.write("Truexp:");
			arq8086.newLine();
			arq8086.write("Mov ax,1;");
			arq8086.newLine();
			arq8086.write("Fimexp:");
			arq8086.newLine();
			arq8086.write("Mov ds:[exp.end], ax");
			arq8086.newLine();

		} else if(token_corrente.token.equals(">=")) {
			CasaToken(">=");
			exps = token_corrente.token;
			tipo2 = EXPS();
			no_Char_op = true;

			arq8086.write("Mov  ax, ds:[exps1.end]");
			arq8086.newLine();
			arq8086.write("Mov  bx, ds:[exps2.end]");
			arq8086.newLine();
			arq8086.write("Cmp ax,bx");
			arq8086.newLine();
			arq8086.write("Jge truexp");
			arq8086.newLine();
			arq8086.write("Mov ax,0;");
			arq8086.newLine();
			arq8086.write("Jmp fimexp");
			arq8086.newLine();
			arq8086.write("Truexp:");
			arq8086.newLine();
			arq8086.write("Mov ax,1;");
			arq8086.newLine();
			arq8086.write("Fimexp:");
			arq8086.newLine();
			arq8086.write("Mov ds:[exp.end], ax");
			arq8086.newLine();

		} else if(token_corrente.token.equals("<=")) {
			CasaToken("<=");
			exps = token_corrente.token;
			tipo2 = EXPS();
			no_Char_op = true;

			arq8086.write("Mov  ax, ds:[exps1.end]");
			arq8086.newLine();
			arq8086.write("Mov  bx, ds:[exps2.end]");
			arq8086.newLine();
			arq8086.write("Cmp ax,bx");
			arq8086.newLine();
			arq8086.write("Jle truexp");
			arq8086.newLine();
			arq8086.write("Mov ax,0;");
			arq8086.newLine();
			arq8086.write("Jmp fimexp");
			arq8086.newLine();
			arq8086.write("Truexp:");
			arq8086.newLine();
			arq8086.write("Mov ax,1;");
			arq8086.newLine();
			arq8086.write("Fimexp:");
			arq8086.newLine();
			arq8086.write("Mov ds:[exp.end], ax");
			arq8086.newLine();
		}

		//System.out.printf("Tipo1: %d - Tipo2: %d - %s\n", tipo, tipo2, exps);

		if(tipo != ERROR) {
			if(tipo2 != ERROR && tipo != tipo2) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}
			if(tipo == TIPO_CHAR && no_Char_op) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}
		}

		return tipo;
	}

  /**
   * Metodo que implementa o procedimento EXPS como especificado pela gramatica.
	 *
	 * @return retorna o tipo do procedimento
   */
	private static int EXPS() throws IOException {
		int tipo = -1;

		if(token_corrente.token.equals("+")) {
			CasaToken("+");
		} else if(token_corrente.token.equals("-")) {
			CasaToken("-");
		}

		tipo = T();

		arq8086.write("Neg ax");
		arq8086.newLine();
		arq8086.write("Mov ds:[t1.end],ax");
		arq8086.newLine();

    while(true) {
			boolean no_Char_op = false; // operacao nao valida para char

			if(token_corrente.token.equals("+")) {
				CasaToken("+");

				arq8086.write("Add ax,bx");
				arq8086.newLine();
				arq8086.write("Mov ds:[exps.end],ax");
				arq8086.newLine();

			} else if(token_corrente.token.equals("-")) {
				CasaToken("-");
				no_Char_op = true;

				arq8086.write("Sub ax,bx");
				arq8086.newLine();
				arq8086.write("Mov ds:[exps.end],ax");
				arq8086.newLine();

			} else if(token_corrente.token.equals("or")) {
				CasaToken("or");
				no_Char_op = true;

				arq8086.write("Or ax,bx");
				arq8086.newLine();
				arq8086.write("Mov ds:[exps.end],ax");
				arq8086.newLine();

			} else {
        break;
      }

			int t = T();

      if(t != ERROR && t != tipo) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}
			if(no_Char_op && tipo == TIPO_CHAR) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}
		}

		return tipo;
	}

  /**
   * Metodo que implementa o procedimento T como especificado pela gramatica.
	 *
	 * @return retorna o tipo do procedimento
   */
	private static int T() throws IOException {
		int tipo = -1;

		tipo = F();

		arq8086.write("Mov  ax, ds:[f1.end]");
		arq8086.newLine();
		arq8086.write("Mov  bx, ds:[f2.end]");
		arq8086.newLine();

		while(true) {
			if(token_corrente.token.equals("*")) {
				CasaToken("*");

				arq8086.write("Imul bx");
				arq8086.newLine();
				arq8086.write("Mov ds:[t.end],ax");
				arq8086.newLine();

			} else if(token_corrente.token.equals("and")) {
				CasaToken("and");

				arq8086.write("and ax,bx");
				arq8086.newLine();
				arq8086.write("Mov ds:[t.end],ax");
				arq8086.newLine();

			} else if(token_corrente.token.equals("/")) {
				CasaToken("/");

				arq8086.write("Idiv bx");
				arq8086.newLine();
				arq8086.write("Mov ds:[t.end],ax");
				arq8086.newLine();

			} else if(token_corrente.token.equals("%")) {
				CasaToken("%");

				arq8086.write("idiv bx");
				arq8086.newLine();
				arq8086.write("Mov ds:[t.end],dx");
				arq8086.newLine();

			} else {
        break;
      }

			int f = F();

      if(f != ERROR && f != tipo) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}
			if (tipo == TIPO_CHAR) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}
		}

		return tipo;
	}

  /**
   * Metodo que implementa o procedimento F como especificado pela gramatica.
	 *
	 * @return retorna o tipo do procedimento
   */
	private static int F() throws IOException {
		int tipo = -1;
		tipo = N();

		arq8086.write("Mov ax, ds:[n.end]");
		arq8086.newLine();
		arq8086.write("Mov ds:[F.end], ax");
		arq8086.newLine();

		while(token_corrente.token.equals("not")) {
			CasaToken("not");
			int n = N();

			arq8086.write("Mov ax, ds:[n.end]");
			arq8086.newLine();
			arq8086.write("Neg ax");
			arq8086.newLine();
			arq8086.write("Mov ds:[n.end], ax");
			arq8086.newLine();

			if(n != ERROR && n != tipo) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}
			if(tipo != -1); // logico? - ERRO
		}

		return tipo;
	}

  /**
   * Metodo que implementa o procedimento N como especificado pela gramatica.
	 *
	 * @return retorna o tipo do procedimento
   */
	private static int N() throws IOException {
		int tipo = -1;

    if(token_corrente.token.equals("(")) {
			CasaToken("(");
			tipo = EXP();
			CasaToken(")");

			arq8086.write("Mov ax, ds:[id.end]");
			arq8086.newLine();
			arq8086.write("Mov ds:[n.end], ax");
			arq8086.newLine();

    } else {
			tipo = CasaTokenConstante();

			arq8086.write("Mov ax, const");
			arq8086.newLine();
			arq8086.write("Mov ds:[n.end], ax");
			arq8086.newLine();

			if(tipo == ERROR && tabela_simbolos.isID(token_corrente.token)) {
				tipo = I();

				arq8086.write("Mov ax, ds:[exp.end]");
				arq8086.newLine();
				arq8086.write("Mov ds:[n.end], ax");
				arq8086.newLine();
			}
		}

		return tipo;
	}

  /**
   * Metodo que implementa o procedimento I como especificado pela gramatica.
	 *
	 * @return retorna o tipo do procedimento
   */
	private static int I() throws IOException {
		int pos = token_corrente.end_tabela;
		int tipo = tabela_simbolos.getID(pos).tipo;

		if(tipo == ERROR) {
			System.out.printf("%d:identificador nao declarado[%s].\n",
												last_line_read, token_corrente.token);
			System.exit(0);
		}

		CasaTokenId();
		if(token_corrente.token.equals("[")) {
			CasaToken("[");

			int pos2 = token_corrente.end_tabela;
			if(CasaTokenConstante() != TIPO_INTEIRO) {
				System.out.printf("%d:tipos incompativeis.\n", last_line_read);
				System.exit(0);
			}
			else {
				int arr_size = 0;

				if(tipo == TIPO_INTEIRO) {
					arr_size = Integer.parseInt(tabela_simbolos.getID(pos2).simbolo);
					tabela_simbolos.getID(pos).tamanho = arr_size;

					if(tabela_simbolos.getID(pos).tamanho > 2000) {
						System.out.printf("%d:tamanho do vetor excede o maximo permitido.\n",
															last_line_read);
						System.exit(0);
					}

					arq8086.write("sword " + (arr_size-1) + " DUP(?)");
					arq8086.newLine();
				}
				if(tipo == TIPO_CHAR) {
					arr_size = Integer.parseInt(tabela_simbolos.getID(pos2).simbolo);
					tabela_simbolos.getID(pos).tamanho = arr_size;

					if(tabela_simbolos.getID(pos).tamanho > 4000) {
						System.out.printf("%d:tamanho do vetor excede o maximo permitido.\n",
															last_line_read);
						System.exit(0);
					}

					arq8086.write("byte " + (arr_size-1) + " DUP(?)");
					arq8086.newLine();
				}
			}

			CasaToken("]");
		}

		return tipo;
	}
}
