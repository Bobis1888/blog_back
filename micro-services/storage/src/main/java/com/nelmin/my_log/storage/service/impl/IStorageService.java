package com.nelmin.my_log.storage.service.impl;

import com.nelmin.my_log.common.dto.HasError;
import com.nelmin.my_log.storage.dto.GetRequestDto;
import com.nelmin.my_log.storage.dto.SaveRequestDto;
import com.nelmin.my_log.storage.dto.StorageResponseDto;
import lombok.NonNull;

public interface IStorageService {
    HasError save(@NonNull SaveRequestDto requestDto);
    StorageResponseDto get(@NonNull GetRequestDto requestDto);
}
