package ru.infosysco.balancer.config;

import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

@Component
public class NotPathRoutePredicateFactory extends PathRoutePredicateFactory {

    @Override
    public Predicate<ServerWebExchange> apply(final Config config) {
        return super.apply(config).negate();
    }

}
