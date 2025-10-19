package com.mycom.myapp.commit.repository;

import com.mycom.myapp.commit.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {
    boolean existsByCommitSha(String commitSha);
}
