public class ParsingException extends RuntimeException {
    public ParsingException(String expected, String got, int line, int col) {
        super(String.format("expected: '%s' found: '%s' in line: %d column: %s", expected, got, line, col),
              new Error(String.format("expected: '%s' found: '%s' in line: %d column: %s", expected, got, line, col)));
    }
}
