package com.nelmin.my_log.storage.service;

import com.nelmin.my_log.common.dto.HasError;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.storage.dto.FacadeStorageResponseDto;
import com.nelmin.my_log.storage.dto.GetRequestDto;
import com.nelmin.my_log.storage.dto.SaveRequestDto;
import com.nelmin.my_log.storage.service.impl.IStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacadeStorageService {

    private final IStorageService storageService;

    @Transactional
    public HasError save(MultipartFile file, String type) {
        log.info("save file: {}", file.getOriginalFilename());

        try {
            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
            String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

            var req = new SaveRequestDto(type, fileName, contentType, file.getBytes(), UUID.randomUUID().toString());

            return storageService.save(req);
        } catch (Exception ex) {
            log.error("Error save file", ex);
            var res = new SuccessDto(false);
            res.reject("internal_error", "save");
            return res;
        }
    }

    @Transactional
    public FacadeStorageResponseDto get(String uuid) {
        return get(uuid, null, null);
    }

    @Transactional
    public FacadeStorageResponseDto get(String uuid, String type, String nickname) {
        try {
            var getReq = new GetRequestDto(type, uuid, nickname);

            var storage = storageService.get(getReq);
            var res = new FacadeStorageResponseDto();

            if (storage == null) {
                res.reject("not_found", "file");
            } else {
                res.setStorageItem(storage);
            }

            return res;
        } catch (Exception ex) {
            log.error("Error get file", ex);
            var res = new FacadeStorageResponseDto();
            res.reject("internal_error", "save");
            return res;
        }
    }
}
