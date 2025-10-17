package com.mycom.myapp.common.exception.custom.gitRepo;

import com.mycom.myapp.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidRepositoryNameException extends RuntimeException {
    private final ErrorCode errorCode;

    public InvalidRepositoryNameException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
