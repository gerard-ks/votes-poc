package ci.org.votespoc.shared.exceptions;

public class UnauthorizedException extends BusinessException {

    public static final String CODE = "UNAUTHORIZED";

    public UnauthorizedException(String message) {
        super(CODE, message);
    }
}
