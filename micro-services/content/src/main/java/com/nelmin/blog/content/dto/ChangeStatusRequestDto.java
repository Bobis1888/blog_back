package com.nelmin.blog.content.dto;

import com.nelmin.blog.content.model.Article;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequestDto(@NotNull Article.Status status) {}
