package com.mycom.myapp.common.exception.custom.gemini;

import com.mycom.myapp.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class GeminiApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public GeminiApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
