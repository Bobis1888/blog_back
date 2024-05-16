package ru.infosysco.balancer.spa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.DispatcherHandler;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Component
@Slf4j
public class SpaGatewayFilterFactory  extends AbstractGatewayFilterFactory<SpaGatewayFilterFactory.Config> {

    private final ObjectProvider<DispatcherHandler> dispatcherHandlerProvider;
    public SpaGatewayFilterFactory(ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
        super(Config.class);
        this.dispatcherHandlerProvider = dispatcherHandlerProvider;  // see ForwardRoutingFilter
    }

    String maskName = "[^.]+\\.[0-9a-z]+\\.[^.]+$";
    String toNotFound = ".*\\.map";

//    @Autowired
//    SpaRoutePredicateFactory spaPredicat;

    @Autowired
    ApplicationContext ctx;

    Map<String, String> uris = new HashMap<>();
    Map<String, String> routersIds = new HashMap<>();

    @EventListener(classes = RefreshRoutesResultEvent.class)
    public void event() {
        RouteLocator  locators = ctx.getBean(RouteLocator.class); // из-за циклической зависимости

        Map<String, String> uris = new HashMap<>();
        locators.getRoutes().subscribe(r -> {

            if (r.getUri().getScheme().equals("file")) {

                uris.put(r.getId(), getCanonicalFolderPath(r.getUri()));
            }
        });

        this.uris.clear();
        this.uris.putAll(uris);
//
//        discoveryClient.getServices()
//                .flatMap(service -> discoveryClient.getInstances(service).collectList())
//                .subscribe((list) -> listMap.put(list.get(0).getServiceId().toLowerCase(), list))
//                .dispose();
    }


    @Override
    public GatewayFilter apply(Config config) {
        // grab configuration from Config object
      //   var predicate = spaPredicat.apply(new SpaRoutePredicateFactory.Config());

        return (exchange, chain) -> {
//            if (!predicate.test(exchange)) {
//                return chain.filter(exchange);
//            }

            Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
            assert route != null;

            String folderPath = this.uris.getOrDefault(route.getId(), getCanonicalFolderPath(route.getUri()));
            // https://www.programmersought.com/article/2469757520/
            // https://www.tabnine.com/code/java/methods/org.springframework.http.server.reactive.ServerHttpResponse/writeAndFlushWith
            String rqPath = null;
            try {
                boolean isIndexHtml = (boolean) exchange.getAttributes().getOrDefault("INDEX_HTML", false);
                rqPath = exchange.getRequest().getPath().value();
                if (isIndexHtml) {
                    rqPath = "index.html";
                }

                Resource resource = getResource(folderPath, rqPath);

                String eTag =  (resource.lastModified() + String.valueOf(resource.contentLength()) );
                if (exchange.checkNotModified(eTag, Instant.ofEpochSecond(resource.lastModified()))) {
                    exchange.getResponse().setStatusCode(HttpStatus.NOT_MODIFIED);
                    return exchange.getResponse().setComplete();
                }

                ServerHttpResponse response = exchange.getResponse();

                MediaType resourceMediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
                exchange.getResponse().getHeaders().setContentType(resourceMediaType);

                // response.writeAndFlushWith("22");
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(resource.getInputStream().readAllBytes());
                exchange.getResponse().setStatusCode(HttpStatus.OK);
                // если в имени фала есть контрольная сумма
                // то unlimit cache
                if (Objects.requireNonNull(resource.getFilename()).matches(maskName)) {
                          exchange.getResponse().getHeaders().setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));
                }

                exchange.getResponse().getHeaders().setETag("\""+ eTag + "\"");
                exchange.getResponse().getHeaders().setLastModified(resource.lastModified());

                return  response.writeWith(Flux.just(buffer));
            } catch (IOException e) {
                // не нашли файл по новой кидаем на выбор роутера
                if (Objects.requireNonNull(rqPath).matches(toNotFound)) {
                    exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                    return exchange.getResponse().setComplete();
                }

                return this.dispatcherHandlerProvider.getIfAvailable().handle(exchange);

//              Ответить 404 not_found
//              exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
//              return exchange.getResponse().setComplete();
            }

        };
    }

    public Resource getResource(String rootFolder, String path) throws IOException {
        File fileLink = new File(rootFolder + File.separator + path);
        return new FileSystemResource(fileLink);
    }

    private String getCanonicalFolderPath(URI uri) {
        if (uri.getScheme().equals("file")) {
          String path = uri.toString().replaceAll("file://", "");
          if (!path.startsWith(".")) {
              path = File.separator + path;
          }
          return path;
        }
        return uri.getPath();
    }

    public static class Config {
        //Put the configuration properties for your filter here
    }
}
