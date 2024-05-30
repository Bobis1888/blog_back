package com.nelmin.blog.common.abstracts;

import lombok.Getter;

@Getter
public final class AnonymousUser implements IUser {
    private final Long id = -1L;
    private final String username = "anonymous";
    private final String password = "****";

    @Override
    public Boolean isEnabled() {
        return true;
    }
}
