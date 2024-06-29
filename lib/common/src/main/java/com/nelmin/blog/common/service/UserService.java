package com.nelmin.blog.common.service;

import com.nelmin.blog.common.dto.UserInfoDto;
import com.nelmin.blog.common.exception.UserNotFoundException;
import com.nelmin.blog.common.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

// TODO user library

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final User.Repo userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {
    }

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
    public void changeNickname(User user, String nickname) {
        var id = userRepository.getIdByNickName(nickname);

        if (id.isPresent() && !id.get().getId().equals(user.getId())) {
            log.info("Nick name {} already taken", nickname);
        } else {
            user.setNickName(nickname);
            userRepository.save(user);
        }
    }

    public String resolveNickname(Long id) {
        return userRepository.getNickNameById(id).orElse(() -> "unknown").getNickName();
    }

    public Long resolveId(String nickname) {
        return userRepository.getIdByNickName(nickname).orElseThrow(UserNotFoundException::new).getId();
    }

    public List<Long> resolveUserIds(String nickName) {

        if (!nickName.startsWith("@")) {
            nickName = "@" + nickName;
        }

        return userRepository.findAllByNickNameContaining(nickName).stream().map(User.UserId::getId).toList();
    }

    @Transactional
    public UserInfoDto info(Long id) {
        var userInfo = userRepository.findById(id);
        var userInfoDto = new UserInfoDto();

        userInfo.ifPresentOrElse((it) -> {
            userInfoDto.setId(it.getId());
            userInfoDto.setNickname(it.getNickName());
            userInfoDto.setEmail(it.getUsername());
            userInfoDto.setEnabled(it.isEnabled());
            userInfoDto.setRegistrationDate(it.getRegistrationDate());
        }, () -> {
            userInfoDto.reject("notFound", "user");
        });

        return userInfoDto;
    }

    @Transactional
    public UserInfoDto publicInfo(String nickname) {
        var user = userRepository.getIdByNickName(nickname);
        AtomicReference<UserInfoDto> userInfoDto = new AtomicReference<>(new UserInfoDto());

        user.ifPresentOrElse((it) -> {
            userInfoDto.set(info(it.getId()));
            userInfoDto.get().setEmail(null);
            userInfoDto.get().setId(null);
        }, () -> {
            userInfoDto.get().reject("notFound", "user");
        });

        return userInfoDto.get();
    }
}
