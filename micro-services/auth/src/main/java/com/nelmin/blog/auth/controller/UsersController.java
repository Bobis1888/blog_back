package com.nelmin.blog.auth.controller;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.dto.SuccessDto;
import com.nelmin.blog.auth.dto.UserInfoDto;
import com.nelmin.blog.auth.model.User;
import com.nelmin.blog.auth.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UsersController {

    private final UserInfo userInfo;
    private final UserService userService;

    @PostConstruct
    private void init() {}

    @GetMapping(value = "/info")
    public ResponseEntity<UserInfoDto> info() {
        User user = (User) userInfo.getCurrentUser();
        return ResponseEntity.ok(user.buildDto());
    }


    @PostMapping(value = "/edit")
    public ResponseEntity<Object> editUserSettings(@Valid @RequestBody UserInfoDto dto) {
        userService.editUserInfo(dto);
        return ResponseEntity.ok(new SuccessDto(true));
    }
}
