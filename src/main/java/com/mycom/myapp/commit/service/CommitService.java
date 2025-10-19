package com.mycom.myapp.commit.service;

import com.mycom.myapp.commit.dto.CommitGroupPageDto;

public interface CommitService {
    CommitGroupPageDto getCommitsSummariesByRepository(Long repositoryId, String githubId, int page, int size);
}
