package services.xml.exception;

/**
 * This type of exception represents an invalid id during parsing, for example a streetSection's id which is not in the previously parse
 * intersections.
 */
public class ParserInvalidIdException extends ParserSemanticException {

    private static final long serialVersionUID = -3443020224287103065L;

    public ParserInvalidIdException(String message) {
        super(message);
    }

    public ParserInvalidIdException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public ParserInvalidIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
