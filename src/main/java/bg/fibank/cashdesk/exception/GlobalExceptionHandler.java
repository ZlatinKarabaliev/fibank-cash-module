package bg.fibank.cashdesk.exception;

import bg.fibank.cashdesk.dto.ErrorResponseDTO;
import bg.fibank.cashdesk.helper.ErrorResponseHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CashOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleCashOperationException(CashOperationException ex, HttpServletRequest request) {
        ErrorResponseDTO response = ErrorResponseHelper.build(
                ex.getMessage(),
                request.getRequestURI(),
                "CASH_OPERATION_ERROR"
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> validationMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponseDTO response = ErrorResponseHelper.buildWithValidation(
                request.getRequestURI(),
                validationMessages
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        ErrorResponseDTO response = ErrorResponseHelper.build(
                ex.getMessage(),
                request.getRequestURI(),
                "UNEXPECTED_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
