/**
  JFlex scanner for NanoMorpho based on a scanner for NanoLisp
  Author: Anton Vilhelm Ásgeirsson, 2020
	NanoLisp scanner Author: Snorri Agnarsson, 2017-2020

	This stand-alone scanner/lexical analyzer can be built and run using:
		java -jar JFlex-full-1.7.0.jar nanolexer.jflex
		javac NanoLexer.java
		java NanoLexer inputfile > outputfile
	Also, the program 'make' can be used with the proper 'makefile':
		make test
 */

import java.io.*;

%%

%public
%class NanoLexer
%unicode
%byaccj

%{

// This part becomes a verbatim part of the program text inside
// the class, NanoLexer.java, that is generated.

// Definitions of tokens:
final static int ERROR = -1;
final static int IF = 1001;
final static int KEYWORD = 1002;
final static int NAME = 1003;
final static int LITERAL = 1004;
final static int AND = 1005;
final static int OR = 1006;
final static int OPNAME1 = 2001;
final static int OPNAME2 = 2002;
final static int OPNAME3 = 2003;
final static int OPNAME4 = 2004;
final static int OPNAME5 = 2005;
final static int OPNAME6 = 2006;
final static int OPNAME7 = 2007;

// A variable that will contain lexemes as they are recognized:
private static String l1, l2;
private static int t1,t2;


public int advance() throws IOException {
    if (this.t2 == 0) {
        this.t1 = 0;
        this.l1 = this.l2;
    } else {
        this.t1 = this.t2;
        this.l1 = this.l2;
        this.t2 = this.yylex();
        this.l2 = this.yytext();
    }
    return this.t1;
}

public void init() throws IOException {
    this.t1 = 999;
    this.t2 = 999;
    this.l1 = "";
    this.l2 = "";
    this.advance();
    this.advance();
}

public int getToken() {
    return this.t1;
}

public int getNextToken() {
    return this.t2;
}

public String getLexeme() {
    return this.l1;
}

// This runs the scanner:
public static void main( String[] args ) throws Exception
{
    NanoLexer lexer = new NanoLexer(new FileReader(args[0]));
    lexer.init();
    while( lexer.getToken()!=0 )
        {
            System.out.println(""+lexer.getToken()+": \'"+lexer.getLexeme()+"\'");
            lexer.advance();
        }
}

%}

/* Reglulegar skilgreiningar */

/* Regular definitions */

_DIGIT=[0-9]
_FLOAT={_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT={_DIGIT}+
_STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
_CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'
_DELIM=[({,;=})]
_AND=&&
_OR=\|\|
_NAME=([:letter:]|{_DIGIT})+
_OPNAME1=[?\~\^]
_OPNAME2=\:
_OPNAME3=\|+
_OPNAME4=&+
_OPNAME5=([<>!]|==|<=|>=)
_OPNAME6=[\+\-]
_OPNAME7=[*/%]
_BOOLOP=[!&|]

%%

/* Lesgreiningarreglur */
/* Scanning rules */

{_DELIM} {
    return yycharat(0);
}

{_AND} {
    return AND;
}

{_OR} {
    return OR;
}

{_STRING} | {_FLOAT} | {_CHAR} | {_INT} | null | true | false {
    return LITERAL;
}

"if" | "elsif" | "else" {
    return IF;
}

"return" | "var" | "while" {
    return KEYWORD;
}

{_OPNAME1} {
    return OPNAME1;
}

{_OPNAME2} {
    return OPNAME2;
}

{_OPNAME3} {
    return OPNAME3;
}

{_OPNAME4} {
    return OPNAME4;
}

{_OPNAME5} {
    return OPNAME5;
}
{_OPNAME6} {
    return OPNAME6;
}
{_OPNAME7} {
    return OPNAME7;
}
{_NAME} {
return NAME;
}

";;;".*$ {
}

[ \t\r\n\f] {
}

. {
return ERROR;
}
