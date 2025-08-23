package uk.gov.hmcts.dev.config;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.hmcts.dev.dto.ResponseData;
import uk.gov.hmcts.dev.dto.ResponseError;
import uk.gov.hmcts.dev.dto.ResponseHandler;
import uk.gov.hmcts.dev.exception.DuplicateException;
import uk.gov.hmcts.dev.util.helper.ErrorHelper;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerConfig {
    private final ErrorHelper errorHelper;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseData<ResponseError<String>>> handleEntityNotFoundExceptionHandler(EntityNotFoundException e){
        return ResponseHandler.generateResponse(// NOSONAR
                errorHelper.fieldValidationFailedError(),
                HttpStatus.NOT_FOUND,
                ResponseError.<String>builder()
                        .error(e.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ResponseData<ResponseError<Map<String, String>>>> handleDuplicateExceptionHandler(DuplicateException e){
        return ResponseHandler.generateResponse(// NOSONAR
                errorHelper.duplicateEntityError(),
                HttpStatus.CONFLICT,
                ResponseError.<Map<String, String>>builder()
                        .errors(Map.of(e.getField(), e.getMsg()))
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<ResponseError<Map<String, String>>>> handleArgumentNotValidExceptionHandler(MethodArgumentNotValidException e){
        var errors = new HashMap<String, String >();
        e.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError)error).getField();
            var errorMessage = error.getDefaultMessage();

            errors.put(fieldName, errorMessage);
        });

        return ResponseHandler.generateResponse(// NOSONAR
                errorHelper.fieldValidationFailedError(),
                HttpStatus.BAD_REQUEST,
                ResponseError.<Map<String, String>>builder()
                        .errors(errors)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<ResponseError<String>>> handleUnexpectedException(Exception e){
        return ResponseHandler.generateResponse(// NOSONAR
                errorHelper.generalError(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseError.<String>builder()
                        .error(errorHelper.unexpectedError())
                        .build()
        );
    }
}


