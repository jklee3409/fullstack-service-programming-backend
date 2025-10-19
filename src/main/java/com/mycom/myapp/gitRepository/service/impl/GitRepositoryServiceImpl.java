package com.mycom.myapp.gitRepository.service.impl;

import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.gitRepo.DuplicationRepositoryException;
import com.mycom.myapp.common.exception.custom.user.UserNotFoundException;
import com.mycom.myapp.gitRepository.dto.request.RegisterRepositoryRequestDto;
import com.mycom.myapp.gitRepository.dto.response.RegisterRepositoryResponseDto;
import com.mycom.myapp.gitRepository.entity.GitRepository;
import com.mycom.myapp.gitRepository.repository.GitRepositoryRepository;
import com.mycom.myapp.gitRepository.service.GitRegisterService;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.webhook.service.GithubWebhookService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitRepositoryServiceImpl implements GitRegisterService {
    private final UserRepository userRepository;
    private final GitRepositoryRepository gitRepositoryRepository;
    private final GithubWebhookService githubWebhookService;

    @Override
    @Transactional
    public RegisterRepositoryResponseDto registerRepository(RegisterRepositoryRequestDto request, String githubId) {
        User user = findUserByGithubId(githubId);
        log.info("[registerRepository] Found user: {}", user.getGithubId());

        gitRepositoryRepository.findByRepoFullName(request.getRepoFullName())
                .ifPresent(r -> {
                    log.info("[registerRepository] 이미 존재하는 리포지토리입니다. RepoFullName: {}", request.getRepoFullName());
                    throw new DuplicationRepositoryException(ErrorCode.ALREADY_REGISTERED_REPOSITORY);
                });

        GithubWebhookService.WebhookResult result = githubWebhookService.createWebhook(request.getRepoFullName(),
                        user.getGithubAccessToken()).block();

        GitRepository gitRepository = GitRepository.builder()
                .user(user)
                .repoFullName(request.getRepoFullName())
                .repoUrl(result.getRepoUrl())
                .webhookId(result.getWebhookId())
                .build();
        gitRepositoryRepository.save(gitRepository);
        log.info("[registerRepository] 새로운 리포지토리를 등록하였습니다. RepoFullName: {}", gitRepository.getRepoFullName());

        return RegisterRepositoryResponseDto.builder()
                .id(gitRepository.getId())
                .repoFullName(gitRepository.getRepoFullName())
                .repoUrl(gitRepository.getRepoUrl())
                .webhookId(gitRepository.getWebhookId())
                .createdAt(gitRepository.getCreatedAt())
                .build();
    }

    private User findUserByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
