package com.nelmin.my_log.content.dto;

import com.nelmin.my_log.content.model.Article;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequestDto(@NotNull Article.Status status) {}
