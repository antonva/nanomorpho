import java.io.*;
public class NanoMorpho {
    public static void main(String[] args) throws IOException {
        NanoLexer lexer = new NanoLexer(new FileReader(args[0]));
        lexer.init();
        lexer.advance();
        while( lexer.getToken()!=0 )
            {
                System.out.println(""+lexer.getToken()+": \'"+lexer.getLexeme()+"\'");
                lexer.advance();
            }
    }
}
