package com.nelmin.blog.auth.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.auth.dto.UserInfoDto;
import com.nelmin.blog.auth.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserInfo userInfo;
    private final User.Repo userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {
        var user = new User();
        user.setUsername("test@test.com");
        user.setNickName("test");
        user.setPassword(passwordEncoder.encode("12345678AA@@aa"));
        userRepository.save(user);
    }

    @Transactional
    public void editUserInfo(UserInfoDto userInfoDto) {
        User user = (User) userInfo.getCurrentUser();
        user.setNickName(userInfoDto.getNickname());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}
