package bg.fibank.cashdesk.helper;

import bg.fibank.cashdesk.dto.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponseHelper {

    public static ErrorResponseDTO build(String message, String path, String errorCode) {
        return new ErrorResponseDTO(LocalDateTime.now(), message, path, errorCode, null);
    }

    public static ErrorResponseDTO buildWithValidation(String path, List<String> validationMessages) {
        return new ErrorResponseDTO(LocalDateTime.now(), "Validation failed", path, "VALIDATION_ERROR", validationMessages);
    }
}

