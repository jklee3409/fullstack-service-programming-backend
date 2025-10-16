package com.mycom.myapp.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GithubLoginRequestDto {
    private String code;
}
