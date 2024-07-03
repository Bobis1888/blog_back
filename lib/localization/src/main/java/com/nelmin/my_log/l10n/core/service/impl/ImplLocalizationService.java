package com.nelmin.my_log.l10n.core.service.impl;

import com.nelmin.my_log.l10n.core.service.LocalizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ImplLocalizationService implements LocalizationService {
    @Override
    public String getDefaultLang() {
        return "";
    }

    @Override
    public void setDefaultLocale(String localeHeader) {

    }
}
