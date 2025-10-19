package com.mycom.myapp.gitRepository.service;

import com.mycom.myapp.gitRepository.dto.request.RegisterRepositoryRequestDto;
import com.mycom.myapp.gitRepository.dto.response.GetRepositoryResponseDto;
import com.mycom.myapp.gitRepository.dto.response.RegisterRepositoryResponseDto;
import com.mycom.myapp.user.entity.User;
import java.util.List;

public interface GitRegisterService {
    RegisterRepositoryResponseDto registerRepository(RegisterRepositoryRequestDto request, String githubId);
    List<GetRepositoryResponseDto> getAllRepositories(String githubId);
}
