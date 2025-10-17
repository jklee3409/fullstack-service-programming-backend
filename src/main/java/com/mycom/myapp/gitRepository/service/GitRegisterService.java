package com.mycom.myapp.gitRepository.service;

import com.mycom.myapp.gitRepository.dto.request.RegisterRepositoryRequestDto;
import com.mycom.myapp.gitRepository.dto.response.RegisterRepositoryResponseDto;
import com.mycom.myapp.user.entity.User;

public interface GitRegisterService {
    RegisterRepositoryResponseDto registerRepository(RegisterRepositoryRequestDto request, String githubId);
}
