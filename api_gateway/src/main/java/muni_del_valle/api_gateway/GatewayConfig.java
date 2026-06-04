package muni_del_valle.api_gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("ms_usuarios", r -> r.path("/api/auth/**")
                    .uri("http://172.20.0.10:8080"))
                .route("ms_reportes", r -> r.path("/api/reports/**")
                    .uri("http://172.20.0.11:8080"))
                .route("ms_monitoreo", r -> r.path("/api/monitor/**")
                    .uri("http://172.20.0.12:8080"))
                .route("ms_alertas", r -> r.path("/api/alerts/**")
                    .uri("http://172.20.0.13:8080"))
                .route("ms_integracion", r -> r.path("/api/integration/**")
                    .uri("http://172.20.0.14:8090"))
                .build();
    }
}
