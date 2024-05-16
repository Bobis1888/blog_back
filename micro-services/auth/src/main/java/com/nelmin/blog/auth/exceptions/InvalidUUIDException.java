package com.nelmin.blog.auth.exceptions;

import com.nelmin.blog.common.exception.CommonException;
import org.springframework.http.HttpStatus;

public class InvalidUUIDException extends CommonException {

    public InvalidUUIDException() {
        this.message = "Invalid UUID";
        this.status = HttpStatus.BAD_REQUEST;
    }
}
