import java.io.*;
import java.util.*;

public class NanoParser {
    private static NanoLexer l;
    private static int varCount;
    private static HashMap<String, Integer> varTable;

    public static Object[] program() throws IOException {
        Vector res = new Vector<Object>();
        while(l.getToken() != 0){
            res.add(function());
        }
        return res.toArray();
    }

    public static Object[] function() throws IOException {
        Vector res = new Vector<Object>();
        varCount = 0;
        varTable = new HashMap<String, Integer>();
        if(l.getToken() != l.NAME){
            throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
        }
        res.add(l.getLexeme());
        l.advance();
        if(l.getToken() != '(') {
            throw new ParsingException("(", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        while(l.getToken() == l.NAME){
            addVar(l.getLexeme());
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
        // Add the function arity to the function signature.
        int arity = varCount;
        res.add(arity);

        if( l.getToken() != ')') {
            throw new ParsingException(")", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        if( l.getToken() != '{') {
            throw new ParsingException("{", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        while(l.getToken() == l.VAR){
            varCount = varCount + decl();
            if( l.getToken() != ';') {
                throw new ParsingException(";", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
        }
        // Add the number of variables
        res.add(varCount);
        Vector exprs = new Vector<Object[]>();
        while( l.getToken() != '}'){
            if(l.getToken() == 0){
                throw new ParsingException("}", l.getLexeme(), l.getLine(), l.getColumn());
            }
            exprs.add(expr());
            if( l.getToken() != ';') {
                throw new ParsingException(";", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
        }
        res.add(exprs.toArray());
        l.advance();
        //TODO: DEBUG REMOVE ME
        System.out.println(varTable);
        return res.toArray();
    }

    public static int decl() throws IOException {
        int vc = 0;
        if(l.getToken() != l.VAR){
            throw new ParsingException("var", l.getLexeme(), l.getLine(), l.getColumn());
        }
        l.advance();
        if(l.getToken() != l.NAME){
            throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
        }

        while(l.getToken() == l.NAME){
            addVar(l.getLexeme());
            l.advance();
            if( l.getToken() == ',') {
                l.advance();
                if(l.getToken() != l.NAME){
                    throw new ParsingException("NAME", l.getLexeme(), l.getLine(), l.getColumn());
                }
            }
        }
        return vc;
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
                res.add("STORE");
                res.add(findVar(l.getLexeme()));
                l.advance();
                l.advance();
                res.add(expr());
            } else {
                return orexpr();
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
            if( l.getNextToken() == '(') {
                res.add("CALL");
                res.add(l.getLexeme());
                l.advance();
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
            } else {
                res.add("FETCH");
                res.add(findVar(l.getLexeme()));
                l.advance();
            }
            return res.toArray();
        } else if (l.getToken() == l.LITERAL) {
            Object[] literal = new Object[]{"LITERAL", l.getLexeme()};
            l.advance();
            return literal;
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
        Vector exprs = new Vector<Object>();
        while( l.getToken() != '}'){
            if(l.getToken() == 0){
                throw new ParsingException("}", l.getLexeme(), l.getLine(), l.getColumn());
            }
            exprs.add(expr());
            if( l.getToken() != ';') {
                throw new ParsingException(";", l.getLexeme(), l.getLine(), l.getColumn());
            }
            l.advance();
        }
        l.advance();
        res.add(exprs.toArray());
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
        printRes(res);
        generateProgram(args[0], res);
        System.out.println("Accepted");
    }

    private static void printRes(Object[] res) {
        for (int i = 0; i < res.length; i++) {
            if (res[i] instanceof Object[]) {
                System.out.print("[");
                printRes((Object[]) res[i]);
                System.out.println("]");
            } else {
                System.out.print(res[i] + ", ");
            }
        }
    }

    static void generateProgram( String filename, Object[] funs ) {
        String programname = filename.substring(0,filename.indexOf('.'));
        System.out.println("\"" + programname +  ".mexe\" = main in");
        System.out.println("!");
        System.out.println("{{");

        for (Object f: funs) {
            generateFunction( (Object[])f );
        }

        System.out.println("}}");
        System.out.println("*");
        System.out.println("BASIS;");
    };

    static void generateFunction( Object[] fun ) {
        //TODO: Label generation for if/while/and/or for short circuit evaluation go gotrue gofalse etc.
        // End results
        // #"g[f1]" =
        int arity = (int) fun[1];
        int varc = (int) fun[2];
        System.out.println("#\"" + fun[0] + "[f" + arity + "]\" =");
        System.out.println("[");
        System.out.println("(MakeVal null)");
        for (int i = 0; i < (arity + varc); i++) {
            System.out.println("(Push)");
        }

        Object[] exprs = (Object[]) fun[3];
        for (int i = 0; i < exprs.length; i++) {
            generateExpr((Object[]) exprs[i]);
        }
        // [
        // .
        // .
        // .
        // ];
        System.out.println("]");
    }

    static void generateExpr( Object[] e ) {
        switch ( (String) e[0]) {
        case "CALL": {
            System.out.println("(" + e[0] + ")");
            return;
        }
        case "STORE": {
            generateExpr((Object[]) e[2]);
            System.out.println("(" + e[0] + " " + e[1] + ")");
            return;
        }
        case "WHILE": {
            System.out.println("(While)");
            return;
        }
        case "FETCH": {
            System.out.println("(Fetch " + e[1] + ")");
            return;
        }
        case "LITERAL": {
            System.out.println("(MakeVal " + e[1] + ")");
            return;
        }
        case "IF": {
            System.out.println("(MakeVal " + e[1] + ")");
            return;
        }
        case "RETURN": {
            generateExpr((Object[]) e[1]);
            System.out.println("(" + e[0] + ")");
            return;
        }
        default: {
            throw new Error((String)e[0]);
        } 
        }
    }

    // Adds a new variable to the symbol table.
    // Throws Error if the variable already exists.
    private static void addVar( String name )
    {
        if( varTable.get(name) != null )
            throw new Error("Variable "+name+" already exists, near line " + l.getLine());
        varTable.put(name,varCount++);
    }

    // Finds the location of an existing variable.
    // Throws Error if the variable does not exist.
    private static int findVar( String name )
    {
        Integer res = varTable.get(name);
        if( res == null )
            throw new Error("Variable "+name+" does not exist, near line " + l.getLine());
        return res;
    }
}
//TODO: Optimizations on condition: generateJump() call 
