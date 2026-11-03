package in.chaukas.web;

import in.chaukas.config.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Dto.ErrorBody> api(ApiException e) {
        return ResponseEntity.status(e.status()).body(new Dto.ErrorBody(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Dto.ErrorBody> invalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + " " + f.getDefaultMessage()).findFirst().orElse("Invalid request.");
        return ResponseEntity.badRequest().body(new Dto.ErrorBody(msg));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Dto.ErrorBody> tooBig(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new Dto.ErrorBody("Image is too large (max 6 MB)."));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ResponseEntity<Dto.ErrorBody> missing(MissingServletRequestPartException e) {
        return ResponseEntity.badRequest().body(new Dto.ErrorBody("Missing part: " + e.getRequestPartName()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Dto.ErrorBody> other(Exception e) {
        log.error("Unhandled error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new Dto.ErrorBody("Something went wrong on our side. Please try again."));
    }
}
