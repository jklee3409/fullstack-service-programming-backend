package com.mycom.myapp.auth.service.impl;

import com.mycom.myapp.auth.config.CustomUserDetails;
import com.mycom.myapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String githubId) throws UsernameNotFoundException {
        return userRepository.findByGithubId(githubId)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with githubId: " + githubId));
    }
}
