package com.nelmin.my_log.user_info.core.impl;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.nelmin.my_log.user_info.core.IUser;

public record AuthenticatedUser(
        Long id,
        @JsonAlias({"email", "username"})
        String username,
        @JsonAlias({"enabled", "isEnabled"})
        Boolean isEnabled,
        Boolean isPremiumUser,
        String nickname,
        Boolean isBlocked) implements IUser {
}
