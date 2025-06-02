package bg.fibank.cashdesk.exception;

import bg.fibank.cashdesk.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized handler for request validation errors.
 */
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationError(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(err -> err instanceof FieldError fe
                        ? fe.getField() + ": " + fe.getDefaultMessage()
                        : err.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponseDTO response = new ErrorResponseDTO(
                LocalDateTime.now(),
                "Validation failed",
                request.getRequestURI(),
                "VALIDATION_ERROR",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericError(Exception ex, HttpServletRequest request) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getRequestURI(),
                "GENERIC_ERROR",
                List.of()
        );
        return ResponseEntity.internalServerError().body(response);
    }
}
