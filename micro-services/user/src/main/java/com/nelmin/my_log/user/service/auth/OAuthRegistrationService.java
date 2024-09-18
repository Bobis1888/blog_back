package com.nelmin.my_log.user.service.auth;

import lombok.NonNull;
import org.springframework.security.core.userdetails.UserDetails;

public interface OAuthRegistrationService {
    UserDetails registration(@NonNull String username);
}
