package com.nelmin.my_log.storage.dto;

import com.nelmin.my_log.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SaveResponseDto extends HasError {
    private final String uuid;
    private final Boolean success;
}
