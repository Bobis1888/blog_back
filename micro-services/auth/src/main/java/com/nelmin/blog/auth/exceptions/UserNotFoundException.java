package com.nelmin.blog.auth.exceptions;

import com.nelmin.blog.common.exception.CommonException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CommonException {

    public UserNotFoundException() {
        this.message = "User not found";
        this.status = HttpStatus.BAD_REQUEST;
    }
}
