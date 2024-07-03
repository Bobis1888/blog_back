package com.nelmin.my_log.storage.dto;

import lombok.NonNull;

public record SaveRequestDto(
        @NonNull String type,
        @NonNull String originalName,
        @NonNull String contentType,
        byte[] file,
        @NonNull String id
) {
}
