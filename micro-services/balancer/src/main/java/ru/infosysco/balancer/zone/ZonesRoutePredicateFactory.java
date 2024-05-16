package ru.infosysco.balancer.zone;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author nelmin
 */
@Slf4j
@Component
public class ZonesRoutePredicateFactory
        extends AbstractRoutePredicateFactory<ZonesRoutePredicateFactory.Config> {

    private final ReactiveDiscoveryClient discoveryClient;
    private final Map<String, List<ServiceInstance>> listMap = new HashMap<>();

    @Value("${server.ssl.enabled:false}")
    private Boolean sslEnabled;

    @Value("${spring.cloud.gateway.white-list:}")
    private String[] whiteList;

    @Value("${spring.cloud.gateway.secure-token:true}")
    private Boolean checkToken;

    public ZonesRoutePredicateFactory(ReactiveDiscoveryClient discoveryClient) {
        super(Config.class);
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
                InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();

                if (remoteAddress != null && remoteAddress.getAddress() != null) {
                    String remoteHostAddress = remoteAddress.getAddress().getHostAddress();
                    Integer remotePort = remoteAddress.getPort();

                    if (Arrays.asList(whiteList).contains(remoteHostAddress)) {
                        log.info("Remote host address in white list {}", remoteHostAddress);
                        return false;
                    }

                    // дев
                    if (sslEnabled) {
                        return false;
                    }

                    Boolean requestFromInternalService = false;
                    String token = exchange.getRequest().getHeaders().getFirst("TOKEN");

                    if (checkToken) {
                        for (var entrySet : listMap.entrySet()) {
                            requestFromInternalService = entrySet
                                    .getValue()
                                    .stream()
                                    .anyMatch(it ->
                                            it.getMetadata().containsKey("token") && it.getMetadata().get("token").equals(token)
                                    );

                            if (requestFromInternalService) {
                                break;
                            }
                        }
                    }

                    if (requestFromInternalService) {

                        return false;
                    }
                    String path = exchange.getRequest().getPath().toString();
                    String destinationServiceId = "";

                    if (!path.isEmpty()) {
                        destinationServiceId = path.replaceFirst("/", "").split("/")[0];
                    }

                    List<ServiceInstance> instances = listMap.getOrDefault(destinationServiceId, new ArrayList<>());

                    if (instances.isEmpty()) {
                        return false;
                    }

                    for (ServiceInstance instance : instances) {
                        Map<String, String> metadata = instance.getMetadata();

                        if (metadata.containsKey("zone")) {
                            log.info("Service zone {}", metadata.get("zone"));
                            return metadata.get("zone").equals("private");
                        }
                    }
                }

                return true;
            };
    }

    @EventListener(classes = RefreshRoutesEvent.class)
    public void event() {
        log.info("RefreshRoutesEvent");

        discoveryClient.getServices()
                .flatMap(service -> discoveryClient.getInstances(service).collectList())
                .subscribe((list) -> listMap.put(list.get(0).getServiceId().toLowerCase(), list))
                .dispose();
    }

    public static class Config {}
}
