package com.mycom.myapp.common.exception.custom.commit;

import com.mycom.myapp.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class CommitNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public CommitNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
