package com.nelmin.blog.common.abstracts;

public interface IUser {
    Long getId();
    String getUsername();
    String getPassword();
    Boolean isEnabled();
}
