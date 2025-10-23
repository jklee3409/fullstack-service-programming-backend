package com.mycom.myapp.commit.service;

import com.mycom.myapp.commit.dto.CommitGroupPageDto;
import com.mycom.myapp.commit.dto.response.GetCommitDetailResponseDto;

public interface CommitService {
    CommitGroupPageDto getCommitsSummariesByRepository(Long repositoryId, String githubId, int page, int size);
    GetCommitDetailResponseDto getCommitDetail(Long commitId, String githubId);
}
