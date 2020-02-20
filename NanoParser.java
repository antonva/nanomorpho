import java.io.*;
public class NanoParser {
    
    public void program(NanoLexer l) throws IOException {
        while(l.getToken() != 0){
            function(l);
        }
    }

    public void function(NanoLexer l) throws IOException {
        if(l.getToken() != 1002){
            throw new Error("expected NAME found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        } 
        l.advance();
        if(!l.getLexeme().equals("(")) {
            throw new Error("expected ( found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
        while(l.getToken() == 1002){
                l.advance();
                if(l.getLexeme().equals(",")){
                    l.advance();
                    if(l.getToken() != 1002){
                        throw new Error("expected NAME found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
                    }
                }
        }                
        if(!l.getLexeme().equals(")")){
            throw new Error("expected ) found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
        if(!l.getLexeme().equals("{")){
            throw new Error("expected { found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
        while(l.getToken() == 1006){
            decl(l);
            if(!l.getLexeme().equals(";")){
                throw new Error("expected ; found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
            }
            l.advance();
        }
        while(!l.getLexeme().equals("}")){
            if(l.getToken() == 0){
                throw new Error("expected } found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
            }
            expr(l);
            if(!l.getLexeme().equals(";")){
                throw new Error("expected ; found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
            }
            l.advance();
        }
        l.advance();
    }

    public void decl(NanoLexer l) throws IOException {
        if(l.getToken() != 1006){
            throw new Error("expected var found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
        if(l.getToken() != 1002){
            throw new Error("expected NAME found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        while(l.getToken() == 1002){
            l.advance();
            if(l.getLexeme().equals(",")){
                l.advance();
                if(l.getToken() != 1002){
                    throw new Error("expected NAME found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
                }
            }
        }
    }

    public void expr(NanoLexer l) throws IOException {
        while(true){
            if(l.getToken() == 1002){
                l.advance();
                if(l.getLexeme().equals("=")){
                    l.advance();
                    expr(l);
                }
                else if(l.getLexeme().equals("(")){
                    l.advance();
                    if(!l.getLexeme().equals(")")){
                        while(true){
                            expr(l);
                            if(l.getLexeme().equals(",")){
                                l.advance();
                            }
                            else if(!l.getLexeme().equals(")")){
                                throw new Error("expected ) found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
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
            }
            else if(l.getToken() == 1008){
                l.advance();
                expr(l);
            }
            else if(l.getToken() >= 2001 && l.getToken() <=2007){
                l.advance();
                expr(l);
            }
            else if(l.getToken() == 1003){
                l.advance();
            }
            else if(l.getLexeme().equals("(")){
                l.advance();
                expr(l);
                if(!l.getLexeme().equals(")")){
                    throw new Error("expected ) found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
                }
                l.advance();
            }
            else if(l.getLexeme().equals("if")){
                ifexpr(l);
            }
            else if(l.getLexeme().equals("while")){
                l.advance();
                if(!l.getLexeme().equals("(")){
                    throw new Error("expected ( found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
                }
                l.advance();
                expr(l);
                if(!l.getLexeme().equals(")")){
                    throw new Error("expected ) found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
                }
                l.advance();
                body(l);
            }
            else{
                throw new Error("illegal expression" + " in line: "+ l.getLine() + " column: "+ l.getColumn());
            }
            if(l.getToken() >= 2001 && l.getToken() <=2007){
                l.advance();
            }
            else{
                break;
            }
        }
    }
    
    public void ifexpr(NanoLexer l) throws IOException {
        if(!l.getLexeme().equals("if")){
            throw new Error("expected if found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
        if(!l.getLexeme().equals("(")){
            throw new Error("expected ( found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
        expr(l);
        if(!l.getLexeme().equals(")")){
            throw new Error("expected ) found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
        body(l);
		elsepart(l);

    }
	
    public void elsepart(NanoLexer l) throws IOException {
		if(l.getLexeme().equals("else")){
            l.advance();
            body(l);
        }
		else if(l.getLexeme().equals("elsif")){
			if(!l.getLexeme().equals("(")){
            throw new Error("expected ( found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
			}
            l.advance();
            expr(l);
			if(!l.getLexeme().equals(")")){
            throw new Error("expected ) found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
			}
            l.advance();
			body(l);
			elsepart(l);
        }
    }
	
	public void body(NanoLexer l) throws IOException {
        if(!l.getLexeme().equals("{")){
            throw new Error("expected { found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
		expr(l);
		if(!l.getLexeme().equals(";")){
                throw new Error("expected ; found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
        }	
        while(!l.getLexeme().equals("}")){
            if(l.getToken() == 0){
                throw new Error("expected } found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
            }
            expr(l);
            if(!l.getLexeme().equals(";")){
                throw new Error("expected ; found: "+ l.getLexeme() + " in line: "+ l.getLine() + " column: "+ l.getColumn());
            }
            l.advance();
        }
        l.advance();
    }
	
	public void opcode(NanoLexer l) throws IOException {
        if(l.getToken() < 2000 || l.getToken() > 2007){
            throw new Error("undocumented instruction '"+ l.getLexeme() + "' in line: "+ l.getLine() + " column: "+ l.getColumn());
        }
        l.advance();
    }
    
    public static void main(String[] args) throws IOException {
        NanoLexer lexer = new NanoLexer(new FileReader(args[0]));
        NanoParser parser = new NanoParser();
        lexer.init();
        parser.program(lexer);
        System.out.println("Accepted");
    }
}
