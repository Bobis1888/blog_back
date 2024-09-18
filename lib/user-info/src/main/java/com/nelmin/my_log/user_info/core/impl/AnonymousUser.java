package com.nelmin.my_log.user_info.core.impl;

import com.nelmin.my_log.user_info.core.IUser;

public final class AnonymousUser implements IUser {

    @Override
    public Long id() {
        return -1L;
    }

    @Override
    public String username() {
        return "anonymous";
    }

    @Override
    public Boolean isEnabled() {
        return true;
    }

    @Override
    public Boolean isPremiumUser() {
        return false;
    }

    @Override
    public String nickname() {
        return "anonymous";
    }

    @Override
    public Boolean isBlocked() {
        return false;
    }
}
