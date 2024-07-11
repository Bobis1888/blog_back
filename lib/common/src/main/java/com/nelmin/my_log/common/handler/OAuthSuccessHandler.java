package com.nelmin.my_log.common.handler;

import com.nelmin.my_log.common.conf.JwtTokenUtils;
import com.nelmin.my_log.common.service.OAuthRegistrationService;
import com.nelmin.my_log.common.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${spring.security.oauth2.client.success_redirect_url:http://localhost:4200/top}")
    private String redirectUrl;

    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final OAuthRegistrationService registrationService;
    private final JwtTokenUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserDetails user = null;
        String userName = extractUserName(authentication.getPrincipal());

        if (!StringUtils.hasText(userName)) {
            log.error("User name not found");
            return;
        }

        try {
            user = userDetailsService.loadUserByUsername(userName);
        } catch (UsernameNotFoundException usernameNotFoundException) {
            log.info("User not found try register");
            user = registrationService.registration(userName);
        } catch (Exception ex) {
            log.error("Error registration", ex);
        }

        if (response.isCommitted()) {
            log.error("Response already committed");
            return;
        }

        if (user == null) {
            log.error("User not found");
            return;
        } else {
            var token = jwtUtils.generateToken(user);
            response.addHeader("Set-Cookie", jwtUtils.createTokenCookieValue(token));
            response.addHeader("Set-Cookie", jwtUtils.createRefreshTokenCookieValue(token));
            userService.updateLastLoginDate(user);
        }

        new DefaultRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String extractUserName(@NonNull Object principal) {

        if (principal instanceof DefaultOAuth2User) {

            try {
                return ((DefaultOAuth2User) principal).getAttribute("email");
            } catch (Exception ex) {
                log.error("Error extract user name", ex);
            }
        }

        return null;
    }
}
