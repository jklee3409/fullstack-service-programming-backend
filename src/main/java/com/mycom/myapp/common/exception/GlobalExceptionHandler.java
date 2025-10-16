package com.mycom.myapp.common.exception;

import com.mycom.myapp.common.dto.base.BaseResponseDto;
import com.mycom.myapp.common.dto.base.ErrorResponseDto;
import com.mycom.myapp.common.exception.custom.CustomJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomJwtException.class)
    public BaseResponseDto<ErrorResponseDto> handleCustomUnsupportedJwtException(CustomJwtException e) {
        log.error("CustomJwtException: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        return BaseResponseDto.fail(e.getErrorCode());
    }
}
