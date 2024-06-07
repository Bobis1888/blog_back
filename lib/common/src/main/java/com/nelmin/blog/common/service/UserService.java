package com.nelmin.blog.common.service;

import com.nelmin.blog.common.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO user library

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final User.Repo userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {}

    @Transactional
    public void changePassword(User user, String password) {
        //TODO validate password
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(User user) {
        //TODO send email
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void changeNickName(User user, String nickName) {
        //TODO check nickName
        user.setNickName(nickName);
        userRepository.save(user);
    }
}
