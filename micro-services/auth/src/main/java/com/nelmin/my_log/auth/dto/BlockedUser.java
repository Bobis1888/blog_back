package com.nelmin.my_log.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlockedUser {
    private String login;
    private LocalDateTime blockDateTime;
    private String reason;
    private Boolean blocked = false;
    private Integer attempts = 0;
}
