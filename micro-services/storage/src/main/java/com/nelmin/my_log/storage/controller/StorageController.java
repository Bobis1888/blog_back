package com.nelmin.my_log.storage.controller;

import com.nelmin.my_log.common.dto.HasError;
import com.nelmin.my_log.storage.dto.FacadeStorageResponseDto;
import com.nelmin.my_log.storage.dto.FileType;
import com.nelmin.my_log.storage.service.FacadeStorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class StorageController {

    private final FacadeStorageService facadeStorageService;

    @Secured("ROLE_USER")
    @PostMapping("/upload")
    public ResponseEntity<HasError> save(@RequestParam("file") MultipartFile file, @RequestParam(value = "type", defaultValue = "TMP") FileType type) {
        var res = facadeStorageService.save(file, type);
        return ResponseEntity.status(res.hasErrors() ? 400 : 200).body(res);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> get(@RequestParam(value = "uuid", defaultValue = "") String uuid, @RequestParam(value = "type", defaultValue = "") FileType type) {
        var res = facadeStorageService.get(uuid, type);

        if (res.hasErrors()) {
            return ResponseEntity.notFound().build();
        }

        return createDownloadResponse(res);
    }

    private ResponseEntity<byte[]> createDownloadResponse(@NonNull FacadeStorageResponseDto storage) {
        byte[] file = storage.getStorageItem().file();

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

        try {
            MediaType contentType = MediaType.parseMediaType(storage.getStorageItem().contentType());
            builder.contentType(contentType);
        } catch (InvalidMediaTypeException ignored) {
            builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        return builder.header("Content-disposition", getContentDispositionHeader()).contentLength(file.length).body(file);
    }

    private String getContentDispositionHeader() {
        String fileName = "file";
        StringBuilder stringBuilder = new StringBuilder("file;");

        stringBuilder.append(String.format(" filename=%1$s;", fileName));

        String encoded = URLEncoder.encode("file", StandardCharsets.UTF_8).replaceAll("\\+", "_");
        stringBuilder.append(String.format(" filename*=UTF-8''%1$s", encoded));

        return stringBuilder.toString();
    }
}
