package com.nelmin.my_log.common.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CommonException {

    public UserNotFoundException() {
        this.message = "User not found";
        this.status = HttpStatus.BAD_REQUEST;
    }
}
