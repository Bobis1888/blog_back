package com.nelmin.my_log.auth.service;

import com.nelmin.my_log.auth.dto.ChangeInfoRequestDto;
import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.common.model.User;
import com.nelmin.my_log.common.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeInfoService {
    private final UserInfo userInfo;
    private final UserService userService;
    private final User.Repo userRepository;


    @Transactional
    public SuccessDto changeNickname(@NonNull ChangeInfoRequestDto dto) {
        var res = new SuccessDto(false);

        if (!StringUtils.hasText(dto.nickname())) {
            res.reject("invalid", "nickname");
            return res;
        }

        try {
            User user = (User) userInfo.getCurrentUser();

            if (StringUtils.hasText(dto.nickname())) {

                // TODO
                if (userRepository.getIdByNickName(dto.nickname()).isPresent()) {
                    res.reject("invalid", "nickname");
                } else {
                    userService.changeNickname(user, dto.nickname());
                }

                res.setSuccess(!res.hasErrors());
            }
        } catch (Exception ex) {
            log.error("Error change nickname", ex);
            res.setSuccess(false);
        }

        return res;
    }

    @Transactional
    public SuccessDto changeDescription(ChangeInfoRequestDto dto) {
        var res = new SuccessDto(false);

        if (!StringUtils.hasText(dto.description())) {
            res.reject("nullable", "description");
        } else {
            try {
                User user = (User) userInfo.getCurrentUser();
                userService.changeDescription(user, dto.description());
                res.setSuccess(true);
            } catch (Exception ex) {
                log.error("Error change description", ex);
                res.setSuccess(false);
            }
        }

        return res;
    }
}
