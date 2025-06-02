package bg.fibank.cashdesk.exception;

/**
 * Custom runtime exception for business logic errors in cash operations.
 */
public class CashOperationException extends RuntimeException {
    public CashOperationException(String message) {
        super(message);
    }

    public CashOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
