package com.mycom.myapp.gitRepository.repository;

import com.mycom.myapp.gitRepository.entity.GitRepository;
import com.mycom.myapp.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitRepositoryRepository extends JpaRepository<GitRepository, Long> {
    Optional<GitRepository> findByRepoFullName(String repoFullName);
    List<GitRepository> findAllByUser(User user);
}
