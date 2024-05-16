package ru.infosysco.balancer.spa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriUtils;

import java.util.List;
import java.util.function.Predicate;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR;

// раздача статики спа
// https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#writing-custom-route-predicate-factories
// https://www.baeldung.com/spring-cloud-gateway-routing-predicate-factories
@Component
public class SpaRoutePredicateFactory extends AbstractRoutePredicateFactory<SpaRoutePredicateFactory.Config> {

    String SPA_ATTRS = "SPA_ATTRS";

    public SpaRoutePredicateFactory() {
        super(Config.class);
    }

    final List<String> toSpaFiles = List.of("apple-app-site-association");

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {

        return exchange -> {

            ServerHttpRequest request = exchange.getRequest();
            String routerId  = exchange.getAttribute(GATEWAY_PREDICATE_ROUTE_ATTR);

            if (routerId != null && routerId.equals(exchange.getAttributes().getOrDefault(SPA_ATTRS, "") ) ) {
                return false;
            }

            exchange.getAttributes().put(SPA_ATTRS, routerId);

            // ForwardRoutingFilter forward при раздаче статики
            String extension = null;
            boolean isIndexHtml =
                    (extension = UriUtils.extractFileExtension( request.getPath().toString())) == null &&
                   request.getMethod().equals(HttpMethod.GET) &&
                   request.getHeaders().getAccept().contains(MediaType.parseMediaType("text/html")) &&
                   request.getHeaders().getContentType() == null;

            if (isIndexHtml) {
                exchange.getAttributes().put("INDEX_HTML", true);
            }

            return isIndexHtml || (extension != null) || matchFixedPath(request.getPath());
        };
    }

    boolean matchFixedPath(RequestPath path) {
        return toSpaFiles.stream().anyMatch(it -> path.contextPath().value().endsWith(it));
    }

    public static class Config {
        //Put the configuration properties for your filter here
    }
}
