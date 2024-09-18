package com.nelmin.my_log.user.service.auth;

import com.nelmin.my_log.user.model.User;
import com.nelmin.my_log.user_info.core.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final User.Repo userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository
                .findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserInfo(user);
    }
}
