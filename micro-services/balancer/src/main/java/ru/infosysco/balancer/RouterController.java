package ru.infosysco.balancer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.CacheControl;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.infosysco.balancer.old_spa.SpaService;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Arrays;

// @RestController
// @PermitAll
// @Component
@RequiredArgsConstructor
public class RouterController {

    private final WebClient.Builder clientBuilder;

    private final ResourceLoader resourceLoader;

    private final ApplicationContext ctx;

    private final RequestUtils requestUtils;

    private final SpaService spa;

    // Раздача index.html
    @SneakyThrows
    public Mono<ServerResponse> rootIndexHtml(ServerRequest request) {
        String host = requestUtils.getHost(request);
        Resource resource = spa.getResource("index.html", host);

        return ServerResponse.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                .body(BodyInserters.fromResource(resource)  );
    }

    // заглушка для браузеров
    @SneakyThrows
    public Mono<ServerResponse> faviconIco(ServerRequest request) {

        return process(request).onErrorResume(
               throwable -> true,
               e -> ServerResponse.ok()
                        .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                        .body(BodyInserters.fromResource(resourceLoader.getResource("classpath:favicon.ico")))
        );
    }

    @SneakyThrows
    public Mono<ServerResponse> processFileSpa(ServerRequest request) {
        String host = requestUtils.getHost(request);

        if(!spa.isEnabled()) {
            return process(request);
        }

        if (!spa.hasResource(request.uri().getPath(), host)) {
            return Mono.error(new FileNotFoundException());
        }

        Resource resource = spa.getResource(request.uri().getPath(), host);
        return ServerResponse.ok()
                // .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))  надо настраивать, файлы в которых есть контрольная сумма можно кешить на год
                .body(BodyInserters.fromResource(resource)  );
    }
    //
    // проксирование WS
    // https://github.com/ji4597056/websocket-forward/tree/master
    // https://www.demo2s.com/java/spring-bodyinserters-fromdatabuffers-t-publisher-inserter-to-write-t.html
    // первый вариант
    @SuppressWarnings("all")
    @SneakyThrows
    public Mono<ServerResponse> process(ServerRequest request) {
        // раздача файлов если есть точка, но это не правильно, сделано просто для примера
        String host = requestUtils.getHost(request);
        String rqPath = request.uri().getPath();

        if (spa.isEnabled() && rqPath.contains(".")) {
            return processFileSpa(request);
        }

        Boolean safeServiceName = rqPath.startsWith("/json"); // надо смотреть конфиг
        if (safeServiceName && rqPath.startsWith("/")) {
            rqPath = "/" + Arrays.stream(rqPath.split("/")).skip(1).findFirst().get() + rqPath;
        }
        String query = "";
        if (request.uri().getQuery() != null) {
            query = "?" + request.uri().getQuery();
        }

        String finalHost = "dev-ubrr-web.infosysco.ru";
        return clientBuilder.build().method(request.method())
            .uri("http:/" + rqPath + query)
            .headers((h) -> h.addAll(request.headers().asHttpHeaders()))
            .headers((h) -> {
                if (finalHost != null) {

                    h.remove("Host");
                    h.add("Host", finalHost);
                }
            })
            .body(BodyInserters.fromDataBuffers(request.bodyToFlux(DataBuffer.class)))
            .exchange()
            .flatMap(cr -> ServerResponse.status(cr.statusCode())
                .cookies(c -> c.addAll(cr.cookies()))
                .headers(hh -> hh.addAll(cr.headers().asHttpHeaders()))
                .body(BodyInserters.fromDataBuffers(cr.bodyToFlux(DataBuffer.class)))
            );


    }

}
