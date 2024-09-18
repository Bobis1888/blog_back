package com.nelmin.my_log.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nelmin.my_log.common.dto.HasError;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoDto extends HasError {
    private Long id;

    @NotBlank(message = "nullable")
    private String nickname;
    private String email;
    private LocalDateTime registrationDate;
    private Boolean enabled;
    private String description;
    private String imagePath;
    private Boolean isPremiumUser;
    private Boolean isBlocked;
    private LocalDateTime premiumExpireDate;
    private Actions actions;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Actions {
        private Boolean subscribe;
        private Boolean unsubscribe;
    }
}
