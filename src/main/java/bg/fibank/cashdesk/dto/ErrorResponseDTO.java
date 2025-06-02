package bg.fibank.cashdesk.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Unified error response structure for the API.
 * Supports error codes and multiple validation errors.
 */
public record ErrorResponseDTO(
        LocalDateTime timestamp,
        String message,
        String path,
        String errorCode,
        List<String> validationMessages
) {}
