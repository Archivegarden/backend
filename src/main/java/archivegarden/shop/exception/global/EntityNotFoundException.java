package archivegarden.shop.exception.global;

public class EntityNotFoundException extends RuntimeException {


    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
