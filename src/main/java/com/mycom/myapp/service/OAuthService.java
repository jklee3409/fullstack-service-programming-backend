package com.mycom.myapp.service;

import com.mycom.myapp.dto.AuthTokens;

public interface OAuthService {
    AuthTokens loginWithGithub(String code);
}
