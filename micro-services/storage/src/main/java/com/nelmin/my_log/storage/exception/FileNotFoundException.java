package com.nelmin.my_log.storage.exception;

import com.nelmin.my_log.common.exception.CommonException;
import org.springframework.http.HttpStatus;

public class FileNotFoundException extends CommonException {

    public FileNotFoundException() {
        this.message = "Invalid UUID";
        this.status = HttpStatus.BAD_REQUEST;
    }
}
