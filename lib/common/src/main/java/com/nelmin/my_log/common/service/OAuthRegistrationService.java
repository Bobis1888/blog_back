package com.nelmin.my_log.common.service;

import lombok.NonNull;
import org.springframework.security.core.userdetails.UserDetails;

public interface OAuthRegistrationService {
    UserDetails registration(@NonNull String username);
}
