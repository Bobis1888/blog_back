package com.nelmin.my_log.common.bean;

import com.nelmin.my_log.common.abstracts.AnonymousUser;
import com.nelmin.my_log.common.abstracts.IUser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Slf4j
@Getter
public class UserInfo implements UserDetails {

    private final IUser currentUser;

    public UserInfo(IUser user) {
        this.currentUser = user;
    }

    public Boolean isAuthorized() {
        return getCurrentUser() != null && !(getCurrentUser() instanceof AnonymousUser);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public Long getId() {
        return currentUser.getId();
    }

    @Override
    public String getPassword() {
        return currentUser.getPassword();
    }

    @Override
    public String getUsername() {
        return currentUser.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return currentUser.isEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


}
