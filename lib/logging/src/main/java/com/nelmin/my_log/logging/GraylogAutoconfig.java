package com.nelmin.my_log.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import de.siegmar.logbackgelf.GelfEncoder;
import de.siegmar.logbackgelf.GelfUdpAppender;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Slf4j
@Configuration
@ConditionalOnExpression("${spring.graylog.enable:true}")
public class GraylogAutoconfig {

    @Value("${spring.graylog.host:localhost}")
    private String graylogHost;

    @Value("${spring.graylog.port:12201}")
    private int graylogPort;

    @Value("${spring.application.name:spring}")
    private String applicationName;

    @Bean
    public LoggerContext loggerContext() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (!graylogHost.isEmpty()) {
            log.info("Graylog enabled");
            log.info("Graylog host: {}, port: {}", graylogHost, graylogPort);
            log.info("Application name: {}", applicationName);

            GelfUdpAppender graylogAppender = new GelfUdpAppender();
            graylogAppender.setName("GELF");
            graylogAppender.setGraylogHost(graylogHost);
            graylogAppender.setGraylogPort(graylogPort);
            graylogAppender.setContext(loggerContext);
            graylogAppender.start();

            var encoder = Optional.ofNullable(graylogAppender.getEncoder()).orElse(new GelfEncoder());
            encoder.addStaticField("applicationName", applicationName);
            encoder.setIncludeLevelName(true);

            AsyncAppender asyncAppender = new AsyncAppender();
            asyncAppender.addAppender(graylogAppender);
            asyncAppender.setContext(loggerContext);
            asyncAppender.start();

            Logger logbackLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            logbackLogger.addAppender(asyncAppender);

            Logger logger = (Logger) LoggerFactory.getLogger("reactor.netty.http.server.AccessLog");
            logger.addAppender(asyncAppender);
            logger.setLevel(Level.DEBUG);
            logger.setAdditive(false);
        }

        return loggerContext;
    }
}
