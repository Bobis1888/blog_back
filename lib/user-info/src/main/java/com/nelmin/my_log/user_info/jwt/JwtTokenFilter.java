package com.nelmin.my_log.user_info.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = jwtTokenUtils.getTokenFromRequest(request);

        try {

            if (jwtTokenUtils.validateToken(token)) {

                String userName = jwtTokenUtils.extractUserName(token);
                UserDetails user = userDetailsService.loadUserByUsername(userName);

                if (user.isAccountNonLocked() && user.isEnabled()) {
                    var authentication = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
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
}
