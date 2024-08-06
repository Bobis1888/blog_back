package com.nelmin.my_log.common.bean;

import com.nelmin.my_log.common.abstracts.AnonymousUser;
import com.nelmin.my_log.common.abstracts.IUser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

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
        // TODO service
        var res = new ArrayList<SimpleGrantedAuthority>();

        if (currentUser instanceof AnonymousUser) {
            res.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        } else {
            res.add(new SimpleGrantedAuthority("ROLE_USER"));

            if (currentUser.isPremiumUser()) {
                res.add(new SimpleGrantedAuthority("ROLE_PREMIUM_USER"));
            }
        }

        return res;
    }

    public Long getId() {
        return currentUser.getId();
    }

    public Boolean isPremiumUser() {
        return currentUser.isPremiumUser();
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
        return !currentUser.isBlocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public String getNickname() {
        return currentUser.getNickName();
    }
}
