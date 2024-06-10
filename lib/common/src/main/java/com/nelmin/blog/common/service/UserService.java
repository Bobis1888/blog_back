package com.nelmin.blog.common.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.dto.UserInfoDto;
import com.nelmin.blog.common.exception.UserNotFoundException;
import com.nelmin.blog.common.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        var id = userRepository.getIdByNickName(nickName);

        if (id.isPresent() && !id.get().getId().equals(user.getId())) {
            log.info("Nick name {} already taken", nickName);
        } else {
            user.setNickName(nickName);
            userRepository.save(user);
        }
    }

    @Transactional
    public UserInfoDto info(Long id) {
        var userInfo = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        var userInfoDto = new UserInfoDto();
        userInfoDto.setId(userInfo.getId());
        userInfoDto.setEmail(userInfo.getUsername());
        userInfoDto.setNickname(userInfo.getNickName());
        userInfoDto.setEnabled(userInfo.isEnabled());
        userInfoDto.setRegistrationDate(userInfo.getRegistrationDate());
        return userInfoDto;
    }
}
