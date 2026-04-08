package com.coupons.profile.infra.resource;

import com.coupons.profile.domain.exception.InvalidReferralCodeException;
import com.coupons.profile.domain.exception.NothingToUpdateException;
import com.coupons.profile.domain.exception.ProfileAlreadyExistsException;
import com.coupons.profile.domain.exception.ProfileNotFoundException;
import com.coupons.profile.domain.exception.ReferralCodeAlreadyUsedException;
import com.coupons.profile.infra.resource.dto.ErrorResponse;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> conflict(ProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(NothingToUpdateException.class)
    public ResponseEntity<ErrorResponse> badRequest(NothingToUpdateException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({InvalidReferralCodeException.class, ReferralCodeAlreadyUsedException.class})
    public ResponseEntity<ErrorResponse> badReferral(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        String msg =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                        .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }
}
