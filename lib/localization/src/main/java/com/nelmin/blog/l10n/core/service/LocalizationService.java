package com.nelmin.blog.l10n.core.service;

public interface LocalizationService {
    String getDefaultLang();

    void setDefaultLocale(String localeHeader);
}
