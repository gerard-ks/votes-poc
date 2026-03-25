package ci.org.votespoc.shared.exceptions;

public class ConflictException extends BusinessException {

    public static final String CODE = "CONFLICT";

    public ConflictException(String message) {
        super(CODE, message);
    }
}
