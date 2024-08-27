package com.nelmin.my_log.storage.service.impl;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.dto.HasError;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.storage.dto.GetRequestDto;
import com.nelmin.my_log.storage.dto.SaveRequestDto;
import com.nelmin.my_log.storage.dto.SaveResponseDto;
import com.nelmin.my_log.storage.dto.StorageResponseDto;
import com.nelmin.my_log.storage.exception.FileNotFoundException;
import com.nelmin.my_log.storage.model.RedisStorage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Service("tmpStorage")
@RequiredArgsConstructor
public class RedisStorageService implements IStorageService {

    private final RedisStorage.Repo storageRepo;
    private final UserInfo userInfo;

    @Override
    public HasError save(@NonNull SaveRequestDto requestDto) {

        try {
            var storage = new RedisStorage();
            storage.setId(requestDto.id());
            storage.setUserId(userInfo.getId());
            storage.setContentType(requestDto.contentType());
            storage.setFile(requestDto.file());
            storageRepo.save(storage);
            return new SaveResponseDto(storage.getId(), true);
        } catch (Exception ex) {
            log.error("Error save file", ex);
            var res = new SuccessDto(false);
            res.reject("internal_error", "save");
            return res;
        }
    }

    @Override
    public StorageResponseDto get(@NonNull GetRequestDto requestDto) {

        try {
            Optional<RedisStorage> storage = Optional.empty();

            if (StringUtils.hasText(requestDto.uuid())) {
                storage = storageRepo.findByUuid(requestDto.uuid());
            }

            var val = storage.orElseThrow(FileNotFoundException::new);
            return new StorageResponseDto(val.getContentType(), val.getFile());
        } catch (Exception ex) {
            log.error("Error get file", ex);
            throw new FileNotFoundException();
        }
    }
}
