package com.nelmin.my_log.content.dto.comment;

import com.nelmin.my_log.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentResponseDto extends HasError {
    private Boolean success;
}
