import java.io.*;
import java.util.*;

public class NanoParser {
    private static NanoLexer l;

    // Definitions of tokens:
    final static int ERROR   = -1;
    final static int IF      = 1001;
    final static int NAME    = 1002;
    final static int LITERAL = 1003;
    final static int AND     = 1004;
    final static int OR      = 1005;
    final static int VAR     = 1006;
    final static int WHILE   = 1007;
    final static int RETURN  = 1008;
    final static int OPNAME1 = 2001;
    final static int OPNAME2 = 2002;
    final static int OPNAME3 = 2003;
    final static int OPNAME4 = 2004;
    final static int OPNAME5 = 2005;
    final static int OPNAME6 = 2006;
    final static int OPNAME7 = 2007;


    public static Object[] program() throws IOException {
        Vector res = new Vector<Object>();
        while(l.getToken() != 0){
            res.add(function());
        }
        return res.toArray();
    }

    public static Object[] function() throws IOException {
        Vector res = new Vector<Object>();
        if(l.getToken() != NAME){
            throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
        }
        res.add(l.getLexeme());
        l.advance();
        if(l.getToken() != '(') {
            throw new ParsingException("(", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        Vector args = new Vector<Object>();
        while(l.getToken() == NAME){
            args.add(l.getLexeme());
            l.advance();
            if( l.getToken() == ',') {
                l.advance();
                if(l.getToken() != NAME){
                    throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
                }
            } else {
                break;
            }
        }
        res.add(args.toArray());
        if( l.getToken() != ')') {
            throw new ParsingException(")", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        if( l.getToken() != '{') {
            throw new ParsingException("{", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        while(l.getToken() == VAR){
            res.add(decl());
            if( l.getToken() != ';') {
                throw new ParsingException(";", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
        }
        while( l.getToken() != '}'){
            if(l.getToken() == 0){
                throw new ParsingException("}", l.getLexeme(), l.getLine(), l.getColumn());
            }
            res.add(expr());
            if( l.getToken() != ';') {
                throw new ParsingException(";", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
        }
        l.advance();
        return res.toArray();
    }

    public static Object[] decl() throws IOException {
        Vector res = new Vector<Object>();
        if(l.getToken() != VAR){
            throw new ParsingException("var", l.getLexeme(), l.getLine(), l.getColumn());
        }
        res.add("STORE");
        l.advance();
        if(l.getToken() != NAME){
            throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
        }
        while(l.getToken() == NAME){
            l.advance();
            if( l.getToken() == ',') {
                l.advance();
                if(l.getToken() != NAME){
                    throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
                }
            }
        }
        return res.toArray();
    }

    public static Object[] expr() throws IOException {
        Vector res = new Vector<Object>();
        switch(l.getToken()) {
        case RETURN:
            res.add("RETURN");
            l.advance();
            res.add(expr());
            return res.toArray();
        case NAME:
            if ( l.getNextToken() == '=' ) {
                res.add("NAME");
                l.advance();
                l.advance();
                res.add(expr());
            } else {
                res.add(orexpr());
            }
            return res.toArray();
        default:
            return orexpr();
        }
    }

    public static Object[] orexpr() throws IOException {
        Vector res = new Vector<Object>();
        Object[] expr1 = andexpr();
        if (l.getToken() == OR ) {
            res.add("OR");
            res.add(expr1);
            l.advance();
            res.add(orexpr());
            return res.toArray();
        }
        return expr1;
    }

    public static Object[] andexpr() throws IOException {
        Vector res = new Vector<Object>();
        Object[] expr1 = notexpr();
        if (l.getToken() == AND ) {
            res.add("AND");
            res.add(expr1);
            l.advance();
            res.add(andexpr());
            return res.toArray();
        }
        return expr1;
    }

    public static Object[] notexpr() throws IOException {
        Vector res = new Vector<Object>();
        if ( l.getToken() == '!') {
            res.add("NOT");
            l.advance();
            res.add(notexpr());
            return res.toArray();
        } else {
            return binopexpr(1);
        }
    }

    public static Object[] binopexpr( int k ) throws IOException {
        if( k == 8 ) return smallexpr();


        Object[] res = binopexpr(k+1);

        // Handle right associative binary operators
        if( k == 2 )
            {
                if( !isOp(l.getToken(),k) ) return res;
                String name = l.getLexeme();
                l.advance();
                Object[] right = binopexpr(k);
                return new Object[]{"CALL",name,new Object[]{res,right}};
            }

        // Handle left associative binary operators
        while( isOp(l.getToken(),k) )
            {
                String name = l.getLexeme();
                l.advance();
                Object[] right = binopexpr(k+1);
                res = new Object[]{"CALL",name,new Object[]{res,right}};
            }

        return res;
    }

    public static Object[] smallexpr() throws IOException {
        Vector res = new Vector<Object>();
        switch(l.getToken()) {
        case IF:
            return ifexpr();
        case NAME:
            res.add("NAME");
            l.advance();
            if( l.getToken() == '(') {
                l.advance();
                while( l.getToken() != ')') {
                    res.add(expr());
                    if( l.getToken() == ',') {
                        l.advance();
                    } else if (l.getToken() == 0 ) {
                        throw new ParsingException(")", l.getLexeme(), l.getLine(), l.getColumn());
                    }
                }
                l.advance();
            }
            return res.toArray();
        case LITERAL:
            res.add(new Object[]{"LITERAL", l.getLexeme()});
            l.advance();
            return res.toArray();
        case WHILE:
            res.add("WHILE");
            l.advance();
            if( l.getToken() == '(') {
                l.advance();
                res.add(expr());
                if( l.getToken() != ')') {
                    throw new ParsingException(")", l.getLexeme(), l.getLine(), l.getColumn());
                }
                l.advance();
                res.add(body());
            } else {
                throw new ParsingException("(", l.getLexeme(), l.getLine(), l.getColumn());
            }
            return res.toArray();
        case 40: //(
            l.advance();
            res.add(expr());
            if ( l.getToken() != ')') {
                throw new ParsingException(")", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
            return res.toArray();
        case OPNAME1: case OPNAME2: case OPNAME3:
        case OPNAME4: case OPNAME5: case OPNAME6:
        case OPNAME7:
            l.advance();
            res.add(smallexpr());
            return res.toArray();
        default:
            throw new ParsingException("SMALLEXPR", l.getLexeme(), l.getLine(), l.getColumn());
        }
    }

    public static Object[] ifexpr() throws IOException {
        Vector res = new Vector<Object>();
        res.add("IF");
        if(!l.getLexeme().equals("if")){
            throw new ParsingException("if", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        if(l.getToken() != '(') {
            throw new ParsingException("(", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        res.add(expr());
        if( l.getToken() != ')') {
            throw new ParsingException(")", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        res.add(body());
        res.add(elsepart());
        return res.toArray();
    }

    public static Object[] elsepart() throws IOException {
        Vector res = new Vector<Object>();
        if(l.getLexeme().equals("else")){
            l.advance();
            res.add(body());
        }
        else if(l.getLexeme().equals("elsif")){
            l.advance();
            if( l.getToken() != '(') {
                throw new ParsingException("(", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
            expr();
            if( l.getToken() != ')') {
                throw new ParsingException(")", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
            body();
            elsepart();
        }
        return res.toArray();
    }

    public static Object[] body() throws IOException {
        Vector res = new Vector<Object>();
        res.add("BODY");
        if( l.getToken() != '{') {
            throw new ParsingException("{", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        while( l.getToken() != '}'){
            if(l.getToken() == 0){
                throw new ParsingException("}", l.getLexeme(), l.getLine(), l.getColumn());
            }
            expr();
            if( l.getToken() != ';') {
                throw new ParsingException(";", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
        }
        l.advance();
        return res.toArray();
    }

    public static boolean isOp( int tok, int k ) {
        switch( tok )
            {
            case OPNAME1:	return k==1;
            case OPNAME2:	return k==2;
            case OPNAME3:	return k==3;
            case OPNAME4:	return k==4;
            case OPNAME5:	return k==5;
            case OPNAME6:	return k==6;
            case OPNAME7:	return k==7;
            default:		return false;
            }
    }

    public static void opcode() throws IOException {
        if(l.getToken() < 2000 || l.getToken() > 2007){
            throw new Error("undocumented instruction '"+ l.getLexeme() + "' in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
    }

    public static void main(String[] args) throws IOException {
        l = new NanoLexer(new FileReader(args[0]));
        l.init();
        Object[] res = program();
        printRes(res,0);
        System.out.println("Accepted");
    }

    private static int printRes(Object[] res, int indent) {
        String tab = "";
        for (int i = 0; i < indent; i++) {
            tab = tab + "";
        }
        System.out.print(tab);
        for (int i = 0; i < res.length; i++) {
            System.out.print("[ ");
            if (res[i] instanceof Object[]) {
                indent++;
                indent = printRes((Object[]) res[i], indent);
            } else {
                System.out.print(res[i] + " ");
            }
        }
        if (indent > 0) {
            indent--;
        }

        if (indent < 3) {
            System.out.println("]");
        } else {
            System.out.print("]");
        }

        return indent;
    }
}
