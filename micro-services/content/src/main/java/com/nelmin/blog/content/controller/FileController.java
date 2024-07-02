package com.nelmin.blog.content.controller;

import com.nelmin.blog.common.dto.SuccessDto;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
public class FileController {

    @Secured("ROLE_USER")
    @PostMapping("/assets/file/upload")
    public ResponseEntity<SuccessDto> save(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        return ResponseEntity.ok(new SuccessDto(true));
    }

    @PostMapping("/assets/file/get/{id}")
    public ResponseEntity<byte[]> get(@PathVariable("id") String id) {

        return createDownloadResponse(new File("/tmp/" + id));
    }

    private ResponseEntity<byte[]> createDownloadResponse(File ss) {
        byte[] file = null;

        try {
            file = Files.readAllBytes(ss.toPath());
        } catch (IOException e) {}

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

        try {
            MediaType contentType = MediaType.parseMediaType(getContentType());
            builder.contentType(contentType);
        } catch (InvalidMediaTypeException ignored) {
            builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        return builder
                .header("Content-disposition", getContentDispositionHeader())
                .contentLength(file.length)
                .body(file);
    }

    private String getContentType() {
        return "image/png";
    }

    private String getContentDispositionHeader() {
        StringBuilder stringBuilder = new StringBuilder("file;");

        stringBuilder.append(String.format(" filename=%1$s;", "file"));

        String encoded = URLEncoder.encode("file", StandardCharsets.UTF_8).replaceAll("\\+", "_");
        stringBuilder.append(String.format(" filename*=UTF-8''%1$s", encoded));

        return stringBuilder.toString();
    }
}
