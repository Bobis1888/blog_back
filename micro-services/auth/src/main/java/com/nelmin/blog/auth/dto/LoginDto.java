package com.nelmin.blog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {

    @NotBlank(message = "nullable")
    private String login;

    @NotBlank(message = "nullable")
    private String password;

    // todo прикрутить защиту от ботов
//    private String capha;
}
