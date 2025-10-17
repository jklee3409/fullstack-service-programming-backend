package com.mycom.myapp.common.exception.custom.github;

import com.mycom.myapp.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class GithubApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public GithubApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
