package com.nelmin.blog.l10n.core.interceptor;

import com.nelmin.blog.l10n.core.service.LocalizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

// TODO
@Slf4j
@Component
@RequiredArgsConstructor
public class LocaleInterceptor implements HandlerInterceptor {
    private static final String LOCALE_SESSION_ATTRIBUTE_NAME = LocaleInterceptor.class.getName() + ".LOCALE";
    private static final String LOCALE_HEADER_NAME = "Locale";

    private final LocalizationService localizationService;
    private final LocaleResolver localeResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String localeHeaderValue = request.getHeader(LOCALE_HEADER_NAME);

        if (StringUtils.hasText(localeHeaderValue)) {
            localizationService.setDefaultLocale(localeHeaderValue);
        }

        WebUtils.setSessionAttribute(request, LOCALE_SESSION_ATTRIBUTE_NAME, localizationService.getDefaultLang());

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
