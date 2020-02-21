NanoParser.class: NanoLexer.class ParsingException.class
	javac NanoParser.java
ParsingException.class:
	javac ParsingException.java
NanoLexer.class: NanoLexer.java
	javac NanoLexer.java
NanoLexer.java: nanolexer.jflex
	java -jar jflex-full-1.7.0.jar nanolexer.jflex
clean:
	rm -Rf *~ NanoLexer*.class NanoLexer.java
test: NanoParser.class fizzbuzz.nm
	java NanoParser fizzbuzz.nm
