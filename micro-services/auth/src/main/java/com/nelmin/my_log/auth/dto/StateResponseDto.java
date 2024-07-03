package com.nelmin.my_log.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nelmin.my_log.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StateResponseDto extends HasError {
    private Boolean logged;
}
