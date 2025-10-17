package com.mycom.myapp.common.exception;

import com.mycom.myapp.common.dto.base.BaseResponseDto;
import com.mycom.myapp.common.dto.base.ErrorResponseDto;
import com.mycom.myapp.common.exception.custom.auth.CustomJwtException;
import com.mycom.myapp.common.exception.custom.gitRepo.DuplicationRepositoryException;
import com.mycom.myapp.common.exception.custom.gitRepo.InvalidRepositoryNameException;
import com.mycom.myapp.common.exception.custom.github.GithubApiException;
import com.mycom.myapp.common.exception.custom.user.UserNotFoundException;
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

    @ExceptionHandler(UserNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(e.getErrorCode());
    }

    @ExceptionHandler(DuplicationRepositoryException.class)
    public BaseResponseDto<ErrorResponseDto> handleDuplicationRepositoryException(DuplicationRepositoryException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(e.getErrorCode());
    }

    @ExceptionHandler(InvalidRepositoryNameException.class)
    public BaseResponseDto<ErrorResponseDto> handleInvalidRepositoryNameException(InvalidRepositoryNameException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(e.getErrorCode());
    }

    @ExceptionHandler(GithubApiException.class)
    public BaseResponseDto<ErrorResponseDto> handleGithubApiException(GithubApiException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(e.getErrorCode());
    }
}
