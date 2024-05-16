package ru.infosysco.balancer.configure;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "server.ssl", name = "enabled", havingValue = "true")
public class HttpToHttpsConfig {

    @Value("${server.port:4433}")
    private int httpsPort;

    @Value("${http.port:8087}")
    private int httpPort;

    private final HttpHandler httpHandler;

    private WebServer runWebServer;

    @PostConstruct
    public void startRedirectServer() {

        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory(httpPort);
        runWebServer = factory.getWebServer(
                (request, response) -> {

                    URI uri = request.getURI();
                    URI httpsUri;

                    try {
                        if (isNeedRedirect(uri.getPath())) {
                            httpsUri = new URI("https",
                                    uri.getUserInfo(),
                                    uri.getHost(),
                                    httpsPort,
                                    uri.getPath(),
                                    uri.getQuery(),
                                    uri.getFragment());
                            response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
                            response.getHeaders().setLocation(httpsUri);
                            return response.setComplete();
                        }

                        return httpHandler.handle(request, response);

                    } catch (URISyntaxException e) {
                        return Mono.error(e);
                    }
                }
        );
        runWebServer.start();

    }

    @PreDestroy
    public void destroyRedirectServer() {

        if (runWebServer == null) {
            return;
        }

        runWebServer.stop();
    }

    private boolean isNeedRedirect(String path) {
        return true;
    }
}
