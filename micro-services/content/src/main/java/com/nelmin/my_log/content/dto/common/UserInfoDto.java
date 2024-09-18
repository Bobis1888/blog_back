package com.nelmin.my_log.content.dto.common;

import java.time.LocalDateTime;

public record UserInfoDto(
        Long id,
        String nickname,
        String email,
        LocalDateTime registrationDate,
        Boolean enabled,
        String description,
        String imagePath,
        Boolean isPremiumUser,
        Boolean isBlocked) {
}
