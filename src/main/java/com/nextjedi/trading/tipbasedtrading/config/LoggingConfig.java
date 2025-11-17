package com.nextjedi.trading.tipbasedtrading.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class LoggingConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public MDCFilter mdcFilter() {
        return new MDCFilter();
    }

    @Component
    @Slf4j
    public static class MDCFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            try {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                String requestId = UUID.randomUUID().toString();
                
                MDC.put("requestId", requestId);
                MDC.put("method", httpRequest.getMethod());
                MDC.put("uri", httpRequest.getRequestURI());
                
                String username = httpRequest.getUserPrincipal() != null ? 
                        httpRequest.getUserPrincipal().getName() : "anonymous";
                MDC.put("username", username);

                chain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        }
    }
}
