package com.mycom.myapp.common.exception.custom.github;

import com.mycom.myapp.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class JsonParsingException extends RuntimeException {
    private final ErrorCode errorCode;

    public JsonParsingException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
