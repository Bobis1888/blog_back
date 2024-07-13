package com.nelmin.my_log.common.abstracts;

public interface IUser {
    Long getId();
    String getUsername();
    String getPassword();
    Boolean isEnabled();
    Boolean isPremiumUser();
}
