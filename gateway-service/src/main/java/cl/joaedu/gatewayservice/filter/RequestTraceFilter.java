package cl.joaedu.gatewayservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Filtro de trazabilidad del Gateway: si la solicitud ya trae X-Request-Id lo respeta
 * (permite seguir una traza entre llamadas encadenadas), si no, genera uno nuevo.
 * Lo agrega tanto a la solicitud que se reenvia al microservicio como a la respuesta
 * final del Gateway, y lo deja en los logs para poder correlacionar una solicitud
 * a traves de Gateway -> microservicio -> logs.
 */
@Component
public class RequestTraceFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final Logger log = LoggerFactory.getLogger(RequestTraceFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        String finalRequestId = requestId;

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(REQUEST_ID_HEADER, finalRequestId)
                .build();

        log.info("[{}] {} {}", finalRequestId, exchange.getRequest().getMethod(), exchange.getRequest().getURI());

        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, finalRequestId);

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
