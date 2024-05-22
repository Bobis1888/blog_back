package com.nelmin.blog.common.conf;

import com.nelmin.blog.common.abstracts.AnonymousUser;
import com.nelmin.blog.common.filer.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {
    private final static String [] PROTECTED_PATHS = {"/user/info"};

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {

        return http
                .getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Bean
    public SecurityFilterChain configure(HttpSecurity http, JwtAuthEntryPoint jwtAuthEntryPoint, JwtTokenFilter jwtTokenFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(it -> it.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .anonymous(it -> it.principal(new AnonymousUser()))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(PROTECTED_PATHS)
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(it -> it
                        .permitAll()
                        .deleteCookies("Authorization", "Refresh")
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((req, resp, auth) -> {
                            resp.setStatus(HttpStatus.OK.value());
                        })
                        .clearAuthentication(true)
                        .invalidateHttpSession(true))
                .build();
    }
}
