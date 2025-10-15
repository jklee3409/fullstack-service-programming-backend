package com.mycom.myapp.service.impl;

import static com.mycom.myapp.entity.enums.Role.*;

import com.mycom.myapp.entity.User;
import com.mycom.myapp.repository.UserRepository;
import com.mycom.myapp.service.UserService;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User getOrRegisterUser(Map<String, Object> userInfo) {
        String githubId = String.valueOf(userInfo.get("id"));
        String nickname = (String) userInfo.get("login");
        String avatarUrl = (String) userInfo.get("avatar_url");

        Optional<User> foundUser = userRepository.findByGithubId(githubId);

        if (foundUser.isPresent()) {
            log.info("User found with GitHub ID: {}", githubId);
            return foundUser.get().update(nickname, avatarUrl);
        } else {
            User newUser = User.builder()
                .githubId(githubId)
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .role(ROLE_USER)
                .build();

            log.info("Registering new user with GitHub ID: {}", githubId);
            return userRepository.save(newUser);
        }
    }
}
