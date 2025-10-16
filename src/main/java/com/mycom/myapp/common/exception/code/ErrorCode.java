package com.mycom.myapp.common.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 10000 ~ 19999: Auth
    UNSUPPORTED_JWT(10000, "UNSUPPORTED_JWT", "지원하지 않는 JWT 토큰입니다."),
    MALFORMED_JWT(10001, "MALFORMED_JWT", "올바르지 않은 JWT 토큰입니다."),
    INVALID_SIGNATURE(10002, "INVALID_SIGNATURE", "잘못된 JWT 서명입니다."),
    EMPTY_JWT_CLAIMS(10003, "EMPTY_JWT_CLAIMS", "JWT claims이 비어있습니다.");

    private final int code;
    private final String name;
    private final String message;
}
