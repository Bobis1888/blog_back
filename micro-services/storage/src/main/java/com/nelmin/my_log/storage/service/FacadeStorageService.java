package com.nelmin.my_log.storage.service;

import com.nelmin.my_log.common.dto.HasError;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.storage.dto.*;
import com.nelmin.my_log.storage.service.impl.IStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FacadeStorageService {

    private final IStorageService tmpStorageService;
    private final IStorageService longStorageService;

    public FacadeStorageService(@Qualifier("tmpStorage") IStorageService tmpStorageService,
                                @Qualifier("longStorage") IStorageService longStorageService) {
        this.tmpStorageService = tmpStorageService;
        this.longStorageService = longStorageService;
    }

    @Transactional
    public HasError save(MultipartFile file, FileType type) {
        log.info("save file: {}", file.getOriginalFilename());

        try {
            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
            String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

            var req = new SaveRequestDto(
                    UUID.randomUUID().toString(),
                    fileName,
                    contentType,
                    type,
                    file.getBytes());

            if (type.equals(FileType.AVATAR)) {
                return longStorageService.save(req);
            } else {
                return tmpStorageService.save(req);
            }
        } catch (Exception ex) {
            log.error("Error save file", ex);
            var res = new SuccessDto(false);
            res.reject("internal_error", "save");
            return res;
        }
    }

    @Transactional
    public FacadeStorageResponseDto get(String uuid, FileType type) {

        try {
            StorageResponseDto storage;
            var req = new GetRequestDto(uuid, type);

            if (type == FileType.TMP) {
                storage = tmpStorageService.get(req);
            } else {
                storage = longStorageService.get(req);
            }

            return new FacadeStorageResponseDto(storage);
        } catch (Exception ex) {
            log.error("Error get file", ex);
            var res = new FacadeStorageResponseDto();
            res.reject("internal_error", "save");
            return res;
        }
    }
}
