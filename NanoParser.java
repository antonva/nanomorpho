import java.io.*;
import java.util.*;

public class NanoParser {
    private static NanoLexer l;

    public static Object[] program() throws IOException {
        Vector res = new Vector<Object>();
        while(l.getToken() != 0){
            res.add(function());
        }
        return res.toArray();
    }

    public static Object[] function() throws IOException {
        Vector res = new Vector<Object>();
        if(l.getToken() != l.NAME){
            throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
        }
        res.add(l.getLexeme());
        l.advance();
        if(l.getToken() != '(') {
            throw new ParsingException("(", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        Vector args = new Vector<Object>();
        while(l.getToken() == l.NAME){
            args.add(l.getLexeme());
            l.advance();
            if( l.getToken() == ',') {
                l.advance();
                if(l.getToken() != l.NAME){
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
        while(l.getToken() == l.VAR){
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
        if(l.getToken() != l.VAR){
            throw new ParsingException("var", l.getLexeme(), l.getLine(), l.getColumn());
        }
        res.add("STORE");
        l.advance();
        if(l.getToken() != l.NAME){
            throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
        }
        while(l.getToken() == l.NAME){
            l.advance();
            if( l.getToken() == ',') {
                l.advance();
                if(l.getToken() != l.NAME){
                    throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
                }
            }
        }
        return res.toArray();
    }

    public static Object[] expr() throws IOException {
        Vector res = new Vector<Object>();
        if ( l.getToken() == l.RETURN) {
            res.add("RETURN");
            l.advance();
            res.add(expr());
            return res.toArray();
        } else if ( l.getToken() == l.NAME) {
            if ( l.getNextToken() == '=' ) {
                res.add("NAME");
                l.advance();
                l.advance();
                res.add(expr());
            } else {
                res.add(orexpr());
            }
            return res.toArray();
        }
        return orexpr();
    }

    public static Object[] orexpr() throws IOException {
        Vector res = new Vector<Object>();
        Object[] expr1 = andexpr();
        if (l.getToken() == l.OR ) {
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
        if (l.getToken() == l.AND ) {
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
        if (l.getToken() == l.IF) {
            return ifexpr();
        } else if (l.getToken() == l.NAME) {
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
        } else if (l.getToken() == l.LITERAL) {
            res.add(new Object[]{"LITERAL", l.getLexeme()});
            l.advance();
            return res.toArray();
        } else if (l.getToken() == l.WHILE) {
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
        } else if (l.getToken() == '(') {
            l.advance();
            res.add(expr());
            if ( l.getToken() != ')') {
                throw new ParsingException(")", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
            return res.toArray();
        } else if (l.getToken() == l.OPNAME1 || l.getToken() == l.OPNAME2 ||
                   l.getToken() == l.OPNAME3 || l.getToken() == l.OPNAME4 ||
                   l.getToken() == l.OPNAME5 || l.getToken() == l.OPNAME6 ||
                   l.getToken() == l.OPNAME7) {
            l.advance();
            res.add(smallexpr());
            return res.toArray();
        }
        throw new ParsingException("SMALLEXPR", l.getLexeme(), l.getLine(), l.getColumn());
    }

    public static Object[] ifexpr() throws IOException {
        Vector res = new Vector<Object>();
        res.add("IF");
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
        if(l.getToken() == l.ELSE){
            l.advance();
            res.add(body());
        }
        else if(l.getToken() == l.ELSIF){
            l.advance();
            if( l.getToken() != '(') {
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
            res.add(expr());
            if( l.getToken() != ';') {
                throw new ParsingException(";", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
        }
        l.advance();
        return res.toArray();
    }

    public static boolean isOp( int tok, int k ) {
        if (tok == l.OPNAME1) { return k==1; }
        if (tok == l.OPNAME2) { return k==2; }
        if (tok == l.OPNAME3) { return k==3; }
        if (tok == l.OPNAME4) { return k==4; }
        if (tok == l.OPNAME5) { return k==5; }
        if (tok == l.OPNAME6) { return k==6; }
        if (tok == l.OPNAME7) { return k==7; }
        return false;
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
        for (int i = 0; i < res.length; i++) {
            if (res[i] instanceof Object[]) {
                System.out.print("[ ");
                indent++;
                indent = printRes((Object[]) res[i], indent);
            } else {
                System.out.print(res[i] + " ");
            }
        }
        if (indent > 0) {
            indent--;
        }

        String tab = "";
        for (int i = 0; i < indent; i++) {
            tab = tab + " ";
        }
        System.out.print("]\n");
        System.out.print(tab);

        return indent;
    }
}
