package com.mycom.myapp.auth.service;

import com.mycom.myapp.auth.dto.AuthTokens;

public interface OAuthService {
    AuthTokens loginWithGithub(String code);
}
