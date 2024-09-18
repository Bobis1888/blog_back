package com.nelmin.my_log.user_info.service;

import com.nelmin.my_log.user_info.core.impl.AuthenticatedUser;
import com.nelmin.my_log.user_info.core.UserInfo;
import com.nelmin.my_log.user_info.jwt.JwtTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * TODO Spring Security Resource Server JWT
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html">Resource Server JWT</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpUserDetailsServer implements UserDetailsService {

    private static final String USER_INFO_PATH = "http://user/info";
    private static final String TOKEN_PREFIX = "Bearer_";

    private final HttpServletRequest request;
    private final JwtTokenUtils jwtTokenUtils;
    private final RestTemplate restTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String token = jwtTokenUtils.getTokenFromRequest(request);

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<AuthenticatedUser> response = null;

        try {
            response = restTemplate.exchange(
                    USER_INFO_PATH,
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    AuthenticatedUser.class);

        } catch (Exception exception) {
            log.error("Error get user info", exception);
            throw new UsernameNotFoundException("User not found");
        }

        return new UserInfo(response.getBody());
    }
}
