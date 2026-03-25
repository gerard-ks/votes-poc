package ci.org.votespoc.shared.exceptions;

public class NotFoundException extends BusinessException {

    public static final String CODE = "NOT_FOUND";

    public NotFoundException(String resource, String identifier) {
        super(CODE, String.format("%s non trouvé: %s", resource, identifier));
    }

    public NotFoundException(String message) {
        super(CODE, message);
    }
}
