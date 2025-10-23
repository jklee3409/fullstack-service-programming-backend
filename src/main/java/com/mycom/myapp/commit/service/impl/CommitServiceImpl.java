package com.mycom.myapp.commit.service.impl;

import com.mycom.myapp.commit.dto.CommitFileDto;
import com.mycom.myapp.commit.dto.CommitGroupDto;
import com.mycom.myapp.commit.dto.CommitGroupPageDto;
import com.mycom.myapp.commit.dto.response.CommitResponseDto;
import com.mycom.myapp.commit.dto.response.GetCommitDetailResponseDto;
import com.mycom.myapp.commit.entity.Commit;
import com.mycom.myapp.commit.entity.CommitFile;
import com.mycom.myapp.commit.repository.CommitFileRepository;
import com.mycom.myapp.commit.repository.CommitRepository;
import com.mycom.myapp.commit.service.CommitService;
import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.commit.CommitNotFoundException;
import com.mycom.myapp.common.exception.custom.gitRepo.RepositoryAccessDeniedException;
import com.mycom.myapp.common.exception.custom.gitRepo.RepositoryNotFoundException;
import com.mycom.myapp.gitRepository.entity.GitRepository;
import com.mycom.myapp.gitRepository.repository.GitRepositoryRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommitServiceImpl implements CommitService {
    private final CommitRepository commitRepository;
    private final CommitFileRepository commitFileRepository;
    private final GitRepositoryRepository gitRepositoryRepository;

    @Override
    @Transactional
    public CommitGroupPageDto getCommitsSummariesByRepository(Long repositoryId, String githubId, int page, int size) {
        GitRepository repository = gitRepositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new RepositoryNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND));
        log.info("[getCommitsSummariesByRepository] 리포지토리 조회 완료. repo: {}", repository.getRepoFullName());

        if (!repository.getUser().getGithubId().equals(githubId)) {
            throw new RepositoryAccessDeniedException(ErrorCode.REPOSITORY_ACCESS_DENIED);
        }

        log.info("[getCommitsSummariesByRepository] 커밋 목록을 조회합니다: repositoryId={}, page={}, size={}, githubId={}", repositoryId, page, size, githubId);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("committedDate").descending());
        Page<Commit> commitPage = commitRepository.findByGitRepositoryId(repositoryId, pageRequest);

        List<CommitResponseDto> commits = commitPage.getContent().stream()
                .map(commit -> CommitResponseDto.builder()
                        .id(commit.getId())
                        .commitSha(commit.getCommitSha())
                        .originalCommitMessage(commit.getOriginalCommitMessage())
                        .authorName(commit.getAuthorName())
                        .committedDate(commit.getCommittedDate())
                        .summary(commit.getSummary())
                        .build())
                .toList();

        // 날짜별 그룹화
        Map<LocalDate, List<CommitResponseDto>> grouped = commits.stream()
                .collect(Collectors.groupingBy(dto -> dto.getCommittedDate().toLocalDate(),
                        LinkedHashMap::new, Collectors.toList()));

        List<CommitGroupDto> groups = grouped.entrySet().stream()
                .map(entry -> CommitGroupDto.builder()
                        .date(entry.getKey())
                        .commits(entry.getValue())
                        .build())
                .toList();

        log.info("[getCommitsSummariesByRepository] 커밋을 날짜별로 그룹화했습니다: {}개 날짜, page={} / totalPages={}",
                groups.size(), commitPage.getNumber(), commitPage.getTotalPages());

        return CommitGroupPageDto.builder()
                .groups(groups)
                .currentPage(commitPage.getNumber())
                .totalPages(commitPage.getTotalPages())
                .hasNext(commitPage.hasNext())
                .build();
    }

    @Override
    public GetCommitDetailResponseDto getCommitDetail(Long commitId, String githubId) {
        Commit commit = commitRepository.findById(commitId)
                .orElseThrow(() -> new CommitNotFoundException(ErrorCode.COMMIT_NOT_FOUND));
        log.info("[getCommitDetail] 커밋 조회 완료. commitSha: {}", commit.getCommitSha());

        GitRepository repository = commit.getGitRepository();
        if (!repository.getUser().getGithubId().equals(githubId)) {
            throw new RepositoryAccessDeniedException(ErrorCode.REPOSITORY_ACCESS_DENIED);
        }

        String commitUrl = repository.getRepoUrl() + "/commit/" + commit.getCommitSha();
        CommitFile commitFile = commitFileRepository.findByCommitId(commitId)
                .orElse(null);
        log.info("[getCommitDetail] 커밋 파일 조회 완료. commitFileId: {}",
                commitFile != null ? commitFile.getId() : "없음");

        return GetCommitDetailResponseDto.builder()
                .commitId(commit.getId())
                .commitSha(commit.getCommitSha())
                .authorName(commit.getAuthorName())
                .authorEmail(commit.getAuthorEmail())
                .originalCommitMessage(commit.getOriginalCommitMessage())
                .summary(commit.getSummary())
                .analysisDetails(commit.getAnalysisDetails())
                .commitUrl(commitUrl)
                .commitFile(CommitFileDto.fromEntity(commitFile))
                .build();
    }
}
