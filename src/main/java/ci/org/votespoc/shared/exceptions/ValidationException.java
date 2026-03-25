package ci.org.votespoc.shared.exceptions;

public class ValidationException extends BusinessException {

    public static final String CODE = "VALIDATION_ERROR";

    public ValidationException(String message) {
        super(CODE, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(CODE, message, cause);
    }
}
