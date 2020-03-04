/**
  JFlex scanner for NanoMorpho based on a scanner for NanoLisp
  Authors: Anton Vilhelm Ásgeirsson <ava7@hi.is>, 2020
           Bjartur Örn Jónsson <boj8@hi.is>, 2020
           Eiður Ingimar Frostason <eif3@hi.is>, 2020

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
%line
%column

%{

// This part becomes a verbatim part of the program text inside
// the class, NanoLexer.java, that is generated.

// Definitions of tokens:
public final static int ERROR   = -1;
public final static int IF      = 1001;
public final static int NAME    = 1002;
public final static int LITERAL = 1003;
public final static int AND     = 1004;
public final static int OR      = 1005;
public final static int VAR     = 1006;
public final static int WHILE   = 1007;
public final static int RETURN  = 1008;
public final static int ELSE  	= 1009;
public final static int ELSIF  	= 1010;
public final static int OPNAME1 = 2001;
public final static int OPNAME2 = 2002;
public final static int OPNAME3 = 2003;
public final static int OPNAME4 = 2004;
public final static int OPNAME5 = 2005;
public final static int OPNAME6 = 2006;
public final static int OPNAME7 = 2007;

// Variables that contain will contain lexemes and tokens as they are recognized.
private static String l1, l2;
private static int t1,t2;
private static int line1,line2;
private static int col1,col2;


public int getLine() { return yyline+1; }
public int getColumn() { return yycolumn; }
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

public String getNextLexeme() {
    return this.l2;
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
_DELIM=[\(\{,;=\}\)]
_AND=&&
_OR=\|\|
_NAME=([:letter:]|{_DIGIT})+
_OPNAME=([?\~\^\:\+\-\|&<>!*/%=])+

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


"if" {
    return IF;
}

"else" {
    return ELSE;
}

"elsif" {
    return ELSIF;
}

"return" {
    return RETURN;
}

"var" {
    return VAR;
}

"while" {
    return WHILE;
}

{_STRING} | {_FLOAT} | {_CHAR} | {_INT} | null | true | false {
    return LITERAL;
}

{_OPNAME} {
    switch(yycharat(0)) {
    case '?': {return OPNAME1;}
    case '~': {return OPNAME1;}
    case '^': {return OPNAME1;}
    case ':': {return OPNAME2;}
    case '|': {return OPNAME3;}
    case '&': {return OPNAME4;}
    case '<': {return OPNAME5;}
    case '>': {return OPNAME5;}
    case '!': {return OPNAME5;}
    case '=': {return OPNAME5;}
    case '+': {return OPNAME6;}
    case '-': {return OPNAME6;}
    case '*': {return OPNAME7;}
    case '/': {return OPNAME7;}
    case '%': {return OPNAME7;}
    default: {return ERROR;}
    }
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
