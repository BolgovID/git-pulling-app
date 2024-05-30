package org.programming.task.gitpullingapp.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.programming.task.gitpullingapp.exception.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;


@Component
@RequiredArgsConstructor
public class AcceptHeaderFilter implements WebFilter {
    private static final String ACCEPT_HEADER = "Accept";
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var acceptHeader = exchange
                .getRequest()
                .getHeaders()
                .getFirst(ACCEPT_HEADER);

        if (Objects.nonNull(acceptHeader) && !acceptHeader.equals(MediaType.APPLICATION_JSON_VALUE)) {
            return handleNotAcceptableHeader(exchange.getResponse(), acceptHeader);
        }
        return chain.filter(exchange);
    }

    private Mono<Void> handleNotAcceptableHeader(ServerHttpResponse response, String acceptHeader) {
        response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        var errorMessage = new ErrorMessage(HttpStatus.NOT_ACCEPTABLE.value(),
                "Accept header" + acceptHeader + " not acceptable");

        try {
            byte[] errorMessageAsByteArray = objectMapper.writeValueAsBytes(errorMessage);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(errorMessageAsByteArray)));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
