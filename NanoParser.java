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
            res.add(expr());
        }
        return res.toArray();
    }

    public static void function() throws IOException {
        if(l.getToken() != NAME){
            throw new Error("expected NAME found: "+ l.getLexeme()
                            + " in line: "+ l.getLine()
                            + " column: "+ l.getColumn());
        }
        l.advance();
        if(!l.getLexeme().equals("(")) {
            throw new Error("expected ( found: "+ l.getLexeme()
                            + " in line: "+ l.getLine()
                            + " column: "+ l.getColumn());
        }
        l.advance();
        while(l.getToken() == NAME){
                l.advance();
                if(l.getLexeme().equals(",")){
                    l.advance();
                    if(l.getToken() != NAME){
                        throw new Error("expected NAME found: "+ l.getLexeme()
                                        + " in line: "+ l.getLine()
                                        + " column: "+ l.getColumn());
                    }
                }
        }
        if(!l.getLexeme().equals(")")){
            throw new Error("expected ) found: "+ l.getLexeme()
                            + " in line: "+ l.getLine()
                            + " column: "+ l.getColumn());
        }
        l.advance();
        if(!l.getLexeme().equals("{")){
            throw new Error("expected { found: "+ l.getLexeme()
                            + " in line: "+ l.getLine()
                            + " column: "+ l.getColumn());
        }
        l.advance();
        while(l.getToken() == VAR){
            decl();
            if(!l.getLexeme().equals(";")){
                throw new Error("expected ; found: "+ l.getLexeme()
                                + " in line: "+ l.getLine()
                                + " column: "+ l.getColumn());
            }
            l.advance();
        }
        while(!l.getLexeme().equals("}")){
            if(l.getToken() == 0){
                throw new Error("expected } found: "+ l.getLexeme()
                                + " in line: "+ l.getLine()
                                + " column: "+ l.getColumn());
            }
            expr();
            if(!l.getLexeme().equals(";")){
                throw new Error("expected ; found: "+ l.getLexeme()
                                + " in line: "+ l.getLine()
                                + " column: "+ l.getColumn());
            }
            l.advance();
        }
        l.advance();
    }

    public static void decl() throws IOException {
        if(l.getToken() != VAR){
            throw new Error("expected var found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
        if(l.getToken() != NAME){
            throw new Error("expected NAME found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        while(l.getToken() == NAME){
            l.advance();
            if(l.getLexeme().equals(",")){
                l.advance();
                if(l.getToken() != NAME){
                    throw new Error("expected NAME found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
                }
            }
        }
    }

    public static Object[] expr() throws IOException {
        Object[] e;
        switch(l.getToken()) {
        case RETURN:
            String ret = l.getLexeme();
            l.advance();
            e = expr();
            return new Object[]{"CALL",ret,e};
        case NAME:
            if (l.getNextToken() == 61 ) {
                String name = l.getLexeme();
                l.advance();
                l.advance();
                e = expr();
                return new Object[]{"CALL",name,e};
            }
        default:
            return orexpr();
        }
    }

    public static Object[] orexpr() throws IOException {
        andexpr();
        if (l.getToken() == OR ) {
            l.advance();
            orexpr();
        }
        return new Object[]{"CALL","x","y"};
    }

    public static void andexpr() throws IOException {
        notexpr();
        if (l.getToken() == AND ) {
            l.advance();
            andexpr();
        }
    }

    public static void notexpr() throws IOException {
        if (l.getLexeme().equals("!")) {
            l.advance();
            notexpr();
        } else {
            binopexpr(1);
        }
    }

    public static void binopexpr( int k ) throws IOException {
        //if( k == 8 ) return smallexpr();
        if( k == 8 ) {
            smallexpr();
            return;
        }


        //Object[] res = binopexpr(k+1);
        binopexpr(k+1);

        // Handle right associative binary operators
        if( k == 2 )
            {
                //if( !isOp(l.getToken(),k) ) return res;
                if( !isOp(l.getToken(),k) ) return;
                String name = l.getLexeme();
                l.advance();
                //Object[] right = binopexpr(k);
                binopexpr(k);
                //return new Object[]{"CALL",name,new Object[]{res,right}};
                return;
            }

        // Handle left associative binary operators
        while( isOp(l.getToken(),k) )
            {
                String name = l.getLexeme();
                l.advance();
                //Object[] right = binopexpr(k+1);
                binopexpr(k+1);
                //res = new Object[]{"CALL",name,new Object[]{res,right}};
            }

        //return res;
    }

    public static void smallexpr() throws IOException {
        switch(l.getToken()) {
        case IF: {
            l.advance();
            ifexpr();
        } break;
        case NAME: {
            String name = l.getLexeme();
            l.advance();
            if(l.getToken() == 41){
                l.advance();
                if(!l.getLexeme().equals(")")){
                    while(true){
                        expr();
                        if(l.getLexeme().equals(",")){
                            l.advance();
                        }
                        else if(!l.getLexeme().equals(")")){
                            throw new Error("expected ) found: "+ l.getLexeme()
                                            + " in line: "+ l.getLine()
                                            + " column: "+ l.getColumn());
                        }
                        else{
                            l.advance();
                            break;
                        }
                    }
                }
                else{
                    l.advance();
                }
            }
        } break;
        case LITERAL: {
            //TODO: Add LITERAL to tree
            l.advance();
        } break;
        case WHILE: {
                l.advance();
                if(!l.getLexeme().equals("(")){
                    throw new Error("expected ( found: " + l.getLexeme()
                                    + " in line: " + l.getLine()
                                    + " column: "+ l.getColumn());
                }
                l.advance();
                expr();
                if(!l.getLexeme().equals(")")){
                    throw new Error("expected ) found: "+ l.getLexeme()
                                    + " in line: "+ l.getLine()
                                    + " column: "+ l.getColumn());
                }
                l.advance();
                body();
        } break;
        case 40: { //(
            l.advance();
            expr();
            if (l.getToken() != 41) { //)
                throw new Error("expected ) found: " + l.getLexeme()
                                + " in line: " + l.getLine()
                                + " column: " + l.getColumn());
            }
            l.advance();
        } break;
        default:
            throw new Error("expected SMALLEXPR found: " + l.getLexeme()
                            + " in line: " + l.getLine()
                            + " column: " + l.getColumn());
        }
    }

    public static void ifexpr() throws IOException {
        if(!l.getLexeme().equals("if")){
            throw new Error("expected if found: "+ l.getLexeme()
                            + " in line: "+ l.getLine()
                            + " column: "+ l.getColumn());
        }
        l.advance();
        if(!l.getLexeme().equals("(")){
            throw new Error("expected ( found: "+ l.getLexeme()
                            + " in line: "+ l.getLine()
                            + " column: "+ l.getColumn());
        }
        l.advance();
        expr();
        if(!l.getLexeme().equals(")")){
            throw new Error("expected ) found: "+ l.getLexeme()
                            + " in line: "+ l.getLine()
                            + " column: "+ l.getColumn());
        }
        l.advance();
        body();
        while(l.getLexeme().equals("elsif")){
            l.advance();
            if(!l.getLexeme().equals("(")){
                throw new Error("expected ( found: "+ l.getLexeme()
                                + " in line: "+ l.getLine()
                                + " column: "+ l.getColumn());
            }
            l.advance();
            expr();
            if(!l.getLexeme().equals(")")){
                throw new Error("expected ) found: "+ l.getLexeme()
                                + " in line: "+ l.getLine()
                                + " column: "+ l.getColumn());
            }
            l.advance();
            body();
        }
        if(l.getLexeme().equals("else")){
            l.advance();
            body();
        }
    }

    public static void body() throws IOException {
        if(!l.getLexeme().equals("{")){
            throw new Error("expected { found: "+ l.getLexeme()
                            + " in line: "+ l.getLine()
                            + " column: "+ l.getColumn());
        }
        l.advance();
        while(!l.getLexeme().equals("}")){
            if(l.getToken() == 0){
                throw new Error("expected } found: "+ l.getLexeme()
                                + " in line: "+ l.getLine()
                                + " column: "+ l.getColumn());
            }
            expr();
            if(!l.getLexeme().equals(";")){
                throw new Error("expected ; found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
            }
            l.advance();
        }
        l.advance();
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

    public static void main(String[] args) throws IOException {
        l = new NanoLexer(new FileReader(args[0]));
        NanoParser parser = new NanoParser();
        l.init();
        Object[] res = parser.program();
        System.out.println("Accepted");
    }
}
