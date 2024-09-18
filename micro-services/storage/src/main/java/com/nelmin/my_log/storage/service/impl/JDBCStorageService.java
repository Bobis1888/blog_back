package com.nelmin.my_log.storage.service.impl;

import com.nelmin.my_log.common.dto.HasError;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.storage.dto.GetRequestDto;
import com.nelmin.my_log.storage.dto.SaveRequestDto;
import com.nelmin.my_log.storage.dto.SaveResponseDto;
import com.nelmin.my_log.storage.dto.StorageResponseDto;
import com.nelmin.my_log.storage.exception.FileNotFoundException;
import com.nelmin.my_log.storage.model.Storage;
import com.nelmin.my_log.user_info.core.UserInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service("longStorage")
@RequiredArgsConstructor
public class JDBCStorageService implements IStorageService {

    private final Storage.Repo storageRepo;
    private final UserInfo userInfo;

    @Override
    public HasError save(@NonNull SaveRequestDto requestDto) {

        try {
            Storage storage;

            if (requestDto.type() != null) {
                storage  = storageRepo.findByUserIdAndType(userInfo.getId(), requestDto.type())
                        .orElse(new Storage());
            } else {
                storage = new Storage();
            }

            storage.setUuid(requestDto.id());
            storage.setUserId(userInfo.getId());
            storage.setType(requestDto.type());
            storage.setContentType(requestDto.contentType());
            storage.setFile(requestDto.file());
            storage.setCreatedDate(LocalDateTime.now());
            storageRepo.save(storage);
            return new SaveResponseDto(storage.getUuid(), true);
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
            Optional<Storage> storage = Optional.empty();

            if (StringUtils.hasText(requestDto.uuid())) {
                storage = storageRepo.findByUuid(requestDto.uuid());
            } else if (requestDto.type() != null) {
                storage = storageRepo.findByUserIdAndType(userInfo.getId(), requestDto.type());
            }

            var val = storage.orElseThrow(FileNotFoundException::new);
            return new StorageResponseDto(val.getContentType(), val.getFile());
        } catch (Exception ex) {
            log.error("Error get file", ex);
            throw new FileNotFoundException();
        }
    }
}
