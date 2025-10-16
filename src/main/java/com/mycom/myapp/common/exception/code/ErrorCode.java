package com.mycom.myapp.common.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ;

    private final int code;
    private final String name;
    private final String message;
}
