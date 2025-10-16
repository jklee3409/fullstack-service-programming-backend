package com.mycom.myapp.common.exception.custom;

import com.mycom.myapp.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class CustomJwtException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomJwtException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
