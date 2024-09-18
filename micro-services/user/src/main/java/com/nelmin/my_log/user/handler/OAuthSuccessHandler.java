package com.nelmin.my_log.user.handler;

import com.nelmin.my_log.user.service.auth.OAuthRegistrationService;
import com.nelmin.my_log.user.service.UserService;
import com.nelmin.my_log.user_info.jwt.JwtTokenUtils;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

        if (!(authentication.getPrincipal() instanceof DefaultOAuth2User)) {
            log.error("Principal is not DefaultOAuth2User");
            return;
        }

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.error("authentication is not OAuth2AuthenticationToken");
            return;
        }

        String authorizedClientRegistrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        String userName = extractUserName((DefaultOAuth2User) authentication.getPrincipal(), authorizedClientRegistrationId);

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

    private String extractUserName(@NonNull DefaultOAuth2User principal, String authorizedClientRegistrationId) {

        try {

            return switch (authorizedClientRegistrationId) {
                case "google" -> principal.getAttribute("email");
                case "yandex" -> principal.getAttribute("default_email");
                default -> null;
            };
        } catch (Exception ex) {
            log.error("Error extract user name", ex);
        }

        return null;
    }
}
