package com.nelmin.my_log.common.filer;

import com.nelmin.my_log.common.conf.JwtTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsService userDetailsService;


    @Autowired
    public JwtTokenFilter(JwtTokenUtils jwtTokenUtils, UserDetailsService userDetailsService) {
        this.jwtTokenUtils = jwtTokenUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        try {

            if (jwtTokenUtils.validateToken(token)) {

                String userName = jwtTokenUtils.extractUserName(token);
                UserDetails user = userDetailsService.loadUserByUsername(userName);

                if (user.isAccountNonLocked() && user.isEnabled()) {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                    clearCookies(response, request);

                    if (request.getSession() != null) {
                        request.getSession().invalidate();
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error filter", ex);
            clearCookies(response, request);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private void clearCookies(HttpServletResponse response, HttpServletRequest request) {
        new CookieClearingLogoutHandler("Authorization", "RefreshToken")
                .logout(request, response, null);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private String getTokenFromRequest(HttpServletRequest request) {

        List<Cookie> cookies = Arrays.asList(request.getCookies() != null ? request.getCookies() : new Cookie[0]);

        if (!cookies.isEmpty()) {

            Optional<Cookie> cookie = cookies
                    .stream()
                    .filter(it -> StringUtils.hasText(it.getValue()) && it.getValue().startsWith("Bearer_"))
                    .findFirst();

            if (cookie.isPresent()) {
                return cookie.get().getValue().substring(7);
            }
        }

        return null;
    }
}
