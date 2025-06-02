package bg.fibank.cashdesk.exception;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard structure for error responses returned by the API.
 */
@Data
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String message;
    private String path;
    private String errorCode;
    private List<String> validationMessages;

    public ErrorResponse(LocalDateTime now, String message, String path, String errorCode, Object o) {
    }
}
