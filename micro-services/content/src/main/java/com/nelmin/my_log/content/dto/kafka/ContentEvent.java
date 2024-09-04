package com.nelmin.my_log.content.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentEvent {
    private Long userId;
    private Type type;
    private String payload;

    public enum Type {
        COMMENT, LIKE, SUBSCRIBE, REPORT
    }
}
