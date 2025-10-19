package com.mycom.myapp.commit.repository;

import com.mycom.myapp.commit.entity.CommitFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitFileRepository extends JpaRepository<CommitFile, Long> {
}
