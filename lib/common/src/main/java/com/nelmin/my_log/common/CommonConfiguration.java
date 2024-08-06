package com.nelmin.my_log.common;

import com.nelmin.my_log.common.abstracts.AnonymousUser;
import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.dto.EmailMessage;
import com.nelmin.my_log.common.service.OAuthRegistrationService;
import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.nelmin", considerNestedRepositories = true)
@ComponentScan(basePackages = "com.nelmin")
public class CommonConfiguration implements WebMvcConfigurer {

    private final List<HandlerInterceptor> interceptorList;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        if (!interceptorList.isEmpty()) {

            for (var interceptor : interceptorList) {
                registry.addInterceptor(interceptor);
            }
        }
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setIncludeClientInfo(false);
        return filter;
    }

    @Bean
    // TODO redis cache
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(
                List.of(
                        new ConcurrentMapCache("default"),
                        new ConcurrentMapCache("users"),
                        new ConcurrentMapCache("tags")
                )
        );
        return cacheManager;
    }

    @Bean
    public Cache cache(CacheManager cacheManager) {
        return cacheManager.getCache("default");
    }

    @Bean
    public Cache usersCache(CacheManager cacheManager) {
        return cacheManager.getCache("users");
    }

    /**
     * The proxyMode attribute is necessary because at the moment of the instantiation of the web application context,
     * there is no active request. Spring creates a proxy to be injected as a dependency,
     * and instantiates the target bean when it is needed in a request.
     *
     * @return UserInfo
     */
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public UserInfo userInfo() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = auth != null ? auth.getPrincipal() : null;

        if (principal == null || auth instanceof AnonymousAuthenticationToken || principal instanceof AnonymousUser) {
            return new UserInfo(new AnonymousUser());
        }

        return (UserInfo) auth.getPrincipal();
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuthRegistrationService defaultRegService() {
        return jwt -> {
            log.debug("Have no any OAuthRegistrationService implementation");
            return null;
        };
    }

    @Bean
    @ConditionalOnMissingBean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public JmsTemplate jmsArtemisTemplate(ConnectionFactory jmsArtemisConnectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(jmsArtemisConnectionFactory);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setMessageConverter(messageConverter());
        return jmsTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageConverter messageConverter() {
        var converter = new MappingJackson2MessageConverter();
        converter.setTypeIdPropertyName("type");
        converter.setTypeIdMappings(Map.of("email", EmailMessage.class));
        return converter;
    }
}
