package ru.infosysco.balancer;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.List;

@Component
public class RequestUtils {

    public String getHost(ServerRequest request) {
        List<String> hosts = request.headers().header("host");
        return !hosts.isEmpty() ? (hosts.get(0)) : null;
    }

    public String getIp(ServerRequest request) {
        List<String> hosts = request.headers().header("host");
        return !hosts.isEmpty() ? (hosts.get(0)) : null;
    }
}
