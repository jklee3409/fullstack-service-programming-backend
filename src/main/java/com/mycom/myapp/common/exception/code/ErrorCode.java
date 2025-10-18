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
    EMPTY_JWT_CLAIMS(10003, "EMPTY_JWT_CLAIMS", "JWT claims이 비어있습니다."),
    MISSING_TOKEN(10004, "MISSING_TOKEN", "토큰이 존재하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(10005, "EXPIRED_ACCESS_TOKEN", "액세스 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(10006, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    INVALID_TOKEN(10007, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),

    // 20000 ~ 29999: User
    USER_NOT_FOUND(20000, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),

    // 30000 ~ 39999: Git Repository
    ALREADY_REGISTERED_REPOSITORY(30000, "ALREADY_REGISTERED_REPOSITORY", "이미 등록된 저장소입니다."),
    INVALID_REPOSITORY_NAME(30001, "INVALID_REPOSITORY_NAME", "올바르지 않은 저장소 이름입니다."),

    // 40000 ~ 49999: Github API
    GITHUB_API_ERROR(40000, "GITHUB_API_ERROR", "깃허브 API 호출 중 오류가 발생했습니다.");

    private final int code;
    private final String name;
    private final String message;
}
