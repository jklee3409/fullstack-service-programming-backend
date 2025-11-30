package com.mycom.myapp.user.service.impl;

import static com.mycom.myapp.auth.entity.enums.Role.ROLE_USER;

import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.user.UserNotFoundException;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.service.UserService;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            log.info("[getOrRegisterUser] 이미 존재하는 사용자입니다 정보를 수정합니다. GitHub ID: {}", githubId);
            return foundUser.get().update(nickname, avatarUrl);
        } else {
            User newUser = User.builder()
                    .githubId(githubId)
                    .nickname(nickname)
                    .avatarUrl(avatarUrl)
                    .role(ROLE_USER)
                    .build();

            log.info("[getOrRegisterUser] 사용자 등록을 완료했습니다. GitHub ID: {}", githubId);
            return userRepository.save(newUser);
        }
    }

    @Transactional
    @Override
    public void updateFcmToken(String githubId, String fcmToken) {
        User user = findUserByGithubId(githubId);
        log.info("[updateFcmToken] 사용자 {} 에 대한 FCM 토큰을 갱신합니다.", user.getId());
        user.updateFcmToken(fcmToken);
    }

    private User findUserByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
