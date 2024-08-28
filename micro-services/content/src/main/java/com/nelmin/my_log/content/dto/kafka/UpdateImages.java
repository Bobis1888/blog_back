package com.nelmin.my_log.content.dto.kafka;

import java.util.List;

public record UpdateImages(
        Long userId,
        List<String> save,
        List<String> remove) {
}
