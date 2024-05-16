package ru.infosysco.balancer.zone;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.DispatcherHandler;
import java.util.*;

/**
 * @author nelmin
 */
@Slf4j
@Component
public class ZonesGatewayFilterFactory extends AbstractGatewayFilterFactory<ZonesGatewayFilterFactory.Config> {

    @Value("${server.ssl.enabled:false}")
    private Boolean sslEnabled;

    @Value("${spring.cloud.gateway.white-list:}")
    private String[] whiteList;

    private final Map<String, List<ServiceInstance>> listMap = new HashMap<>();

    public ZonesGatewayFilterFactory(ReactiveDiscoveryClient discoveryClient, ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        };
    }

    public static class Config {
    }
}
