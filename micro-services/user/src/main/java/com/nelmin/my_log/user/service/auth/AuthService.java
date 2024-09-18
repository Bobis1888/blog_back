package com.nelmin.my_log.user.service.auth;

import com.nelmin.my_log.user.dto.AuthResponseDto;
import com.nelmin.my_log.user.dto.LoginRequestDto;
import com.nelmin.my_log.common.exception.UserNotFoundException;
import com.nelmin.my_log.user.service.BlockService;
import com.nelmin.my_log.user.service.UserService;
import com.nelmin.my_log.user_info.core.UserInfo;
import com.nelmin.my_log.user_info.jwt.JwtTokenUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserInfo userInfo;
    private final UserService userService;
    private final BlockService blockService;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDto authenticate(LoginRequestDto loginRequestDto) {
        var authResponse = new AuthResponseDto(false);

        if (userInfo.isAuthorized()) {
            log.info("User {} already authorized", loginRequestDto.login());
            authResponse.reject("logged", "login");
            return authResponse;
        }

        blockService.check(loginRequestDto, authResponse);

        if (authResponse.hasErrors()) {
            return authResponse;
        }

        UserDetails userInfo;

        try {
            userInfo = userDetailsService.loadUserByUsername(loginRequestDto.login());

            if (!userInfo.isEnabled()) {
                throw new UserNotFoundException();
            }

            if (!userInfo.isAccountNonLocked()) {
                authResponse.reject("locked", "login");
                return authResponse;
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequestDto.login(), loginRequestDto.password());
            authentication = authenticationManager.authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException | UsernameNotFoundException | UserNotFoundException exception) {
            authResponse.reject("invalid", "password");
            authResponse.reject("invalid", "login");
            return authResponse;
        } catch (Exception exception) {
            log.error("Auth Error", exception);
            authResponse.reject("serverError");
            return authResponse;
        }

        userService.updateLastLoginDate(userInfo);
        var token = jwtTokenUtils.generateToken(userInfo);
        authResponse.setToken(token);
        authResponse.setRefreshToken(token);
        authResponse.setSuccess(true);

        blockService.clean(loginRequestDto.login());
        log.info("User {} logged", userInfo.getUsername());
        return authResponse;
    }
}


