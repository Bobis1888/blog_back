package com.nelmin.my_log.storage.dto;

import lombok.NonNull;

public record SaveRequestDto(
        @NonNull String id,
        @NonNull String originalName,
        @NonNull String contentType,
        FileType type,
        byte[] file
) {
}
