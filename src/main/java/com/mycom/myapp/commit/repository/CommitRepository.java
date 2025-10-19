package com.mycom.myapp.commit.repository;

import com.mycom.myapp.commit.entity.Commit;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {
    boolean existsByCommitSha(String commitSha);
    Page<Commit> findByGitRepositoryId(Long repositoryId, Pageable pageable);
}
