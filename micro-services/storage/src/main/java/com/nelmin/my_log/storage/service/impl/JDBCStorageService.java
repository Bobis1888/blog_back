package com.nelmin.my_log.storage.service.impl;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.dto.HasError;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.common.service.UserService;
import com.nelmin.my_log.storage.dto.GetRequestDto;
import com.nelmin.my_log.storage.dto.SaveRequestDto;
import com.nelmin.my_log.storage.dto.SaveResponseDto;
import com.nelmin.my_log.storage.dto.StorageResponseDto;
import com.nelmin.my_log.storage.model.Storage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class JDBCStorageService implements IStorageService {

    private final Storage.Repo storageRepo;
    private final UserInfo userInfo;
    private final UserService userService;

    @Override
    public HasError save(@NonNull SaveRequestDto requestDto) {

        try {

            if (typeIsUnique(requestDto.type())) {
                storageRepo.deleteByTypeAndUserId(requestDto.type(), userInfo.getId());
            }

            var storage = new Storage();
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
            Storage storage = null;

            if (StringUtils.hasText(requestDto.uuid())) {
                storage = storageRepo.findByUuid(requestDto.uuid()).orElse(null);
            }

            if (StringUtils.hasText(requestDto.type())) {
                Long userId = userInfo.getId();

                if (StringUtils.hasText(requestDto.nickname())) {
                    userId = userService.resolveId(requestDto.nickname());
                }

                storage = storageRepo.findByTypeAndUserId(requestDto.type(), userId).orElse(null);
            }

            if (storage == null) {
                log.error("File not found");
                return null;
            } else {
                return new StorageResponseDto(storage.getContentType(), storage.getFile());
            }

        } catch (Exception ex) {
            log.error("Error get file", ex);
            return null;
        }
    }

    // TODO
    private Boolean typeIsUnique(String type) {
        return Objects.equals("avatar", type);
    }
}
