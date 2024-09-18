package com.nelmin.my_log.user_info.core;

public interface IUser {
    Long id();
    String username();
    Boolean isEnabled();
    Boolean isPremiumUser();
    String nickname();
    Boolean isBlocked();

    default String password() {
        return "";
    }
}
