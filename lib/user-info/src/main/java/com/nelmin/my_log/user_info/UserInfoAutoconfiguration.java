package com.nelmin.my_log.user_info;

import com.nelmin.my_log.user_info.core.impl.AnonymousUser;
import com.nelmin.my_log.user_info.core.UserInfo;
import com.nelmin.my_log.user_info.jwt.JwtAuthEntryPoint;
import com.nelmin.my_log.user_info.jwt.JwtTokenFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class UserInfoAutoconfiguration {

    /**
     * The proxyMode attribute is necessary because at the moment of the instantiation of the web application context,
     * there is no active request. Spring creates a proxy to be injected as a dependency,
     * and instantiates the target bean when it is needed in a request.
     *
     * @return UserInfo
     */
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    @ConditionalOnMissingBean
    public UserInfo userInfo() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = auth != null ? auth.getPrincipal() : null;

        if (principal == null || auth instanceof AnonymousAuthenticationToken || principal instanceof AnonymousUser) {
            return new UserInfo(new AnonymousUser());
        }

        return (UserInfo) auth.getPrincipal();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                         JwtAuthEntryPoint jwtAuthEntryPoint,
                                         JwtTokenFilter jwtTokenFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(it -> it.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .anonymous(it -> it.principal(new AnonymousUser()))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(reg -> reg
                        .anyRequest()
                        .permitAll())

                .build();
    }

    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
