package com.nelmin.my_log.common.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.dto.UserInfoDto;
import com.nelmin.my_log.common.exception.UserNotFoundException;
import com.nelmin.my_log.common.model.User;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    @CacheEvict(value = "users", key = "#user.id")
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

    @Cacheable(value = "users", key = "#id")
    public String resolveNickname(Long id) {
        return userRepository.getNickNameById(id)
                .orElse(() -> "unknown")
                .getNickName();
    }

    public Map<Long, String> resolveNicknames(List<Long> ids) {
        return userRepository.getIdsAndNickNamesByIdIn(ids)
                .stream()
                .collect(
                        Collectors.toMap(
                                User.UserIdAndNickName::getId,
                                User.UserIdAndNickName::getNickName
                        )
                );
    }

    public Long resolveId(String nickname) {
        return userRepository.getIdByNickName(nickname)
                .orElseThrow(UserNotFoundException::new)
                .getId();
    }

    public List<Long> resolveUserIds(String nickName) {

        if (!nickName.startsWith("@")) {
            nickName = "@" + nickName;
        }

        return userRepository.findAllByNickNameContaining(nickName)
                .stream()
                .map(User.UserId::getId)
                .toList();
    }

    @Transactional
    public UserInfoDto info(Long id) {
        var userInfo = userRepository.findById(id);
        var userInfoDto = new UserInfoDto();

        userInfo.ifPresentOrElse((it) -> {
            userInfoDto.setId(it.getId());
            userInfoDto.setNickname(it.getNickName());
            userInfoDto.setEmail(it.getUsername());
            userInfoDto.setRegistrationDate(it.getRegistrationDate());
            userInfoDto.setEnabled(it.isEnabled());
            userInfoDto.setDescription(it.getDescription());
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
        }, () -> userInfoDto.get().reject("notFound", "user"));

        return userInfoDto.get();
    }

    @Transactional
    public void changeDescription(@NonNull User user, @NonNull String description) {
        user.setDescription(description);
        userRepository.save(user);
    }


    @Transactional
    public void updateLastLoginDate(@NonNull UserDetails userInfo) {

        if (!(userInfo instanceof UserInfo)) {
            return;
        }

        var user = ((User) ((UserInfo) userInfo).getCurrentUser());
        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);
    }
}
