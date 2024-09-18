package com.nelmin.my_log.user.controller;

import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.user.dto.UserInfoDto;
import com.nelmin.my_log.user.service.UserService;
import com.nelmin.my_log.user.dto.ChangeInfoRequestDto;
import com.nelmin.my_log.user.service.ChangeInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class InfoController {

    private final UserService userService;
    private final ChangeInfoService changeInfoService;

    @Secured("ROLE_USER")
    @GetMapping(value = "/info")
    public ResponseEntity<UserInfoDto> info() {
        var response = userService.info();

        return ResponseEntity.status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/info/{nickname}")
    public ResponseEntity<List<UserInfoDto>> infos(@Valid @PathVariable String nickname) {
        var response = userService.publicInfo(nickname);
        return ResponseEntity.status(response.isEmpty() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    // TODO refactor /change-* methods

    @Secured("ROLE_USER")
    @PostMapping(value = "/change-nickname")
    public ResponseEntity<SuccessDto> changeNickname(@Valid @RequestBody ChangeInfoRequestDto dto) {
        var response = changeInfoService.changeNickname(dto);

        return ResponseEntity.status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/change-image-path")
    public ResponseEntity<SuccessDto> changeImagePath(@Valid @RequestBody ChangeInfoRequestDto dto) {
        var response = changeInfoService.changeImagePath(dto);

        return ResponseEntity.status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/change-description")
    public ResponseEntity<SuccessDto> changeDescription(@Valid @RequestBody ChangeInfoRequestDto dto) {
        var response = changeInfoService.changeDescription(dto);

        return ResponseEntity.status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/info/resolve_nicknames")
    public ResponseEntity<Map<Long, String>> resolveNicknames(@Valid @RequestParam List<Long> ids) {
        var response = userService.resolveNicknames(ids);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
