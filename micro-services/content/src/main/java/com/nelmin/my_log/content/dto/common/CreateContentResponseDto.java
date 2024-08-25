package com.nelmin.my_log.content.dto.common;

import com.nelmin.my_log.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateContentResponseDto extends HasError {
    private Boolean success;
    private Long id;
}
