package com.rxonda;

import java.util.concurrent.Executors;

import com.rxonda.handler.PersonHandler;
import com.rxonda.repo.PersonRepository;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.http.MediaType.*;

@Configuration
class WebConfiguration {
    @Bean
    Scheduler jdbcScheduler(Environment env) {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(env.getRequiredProperty("jdbc.connection.pool.size", Integer.class)));
    }

    @Bean
    PersonHandler personHandler(PersonRepository personRepository, Scheduler jdbcScheduler) {
        return new PersonHandler(personRepository, jdbcScheduler);
    }

    @Bean
    RouterFunction<ServerResponse> routerFunction(PersonHandler personHandler) {
        return RouterFunctions
            .route(GET("/person").and(accept(APPLICATION_JSON)), personHandler::list)
            .andRoute(GET("/person/{id}").and(accept(APPLICATION_JSON)), personHandler::show)
            .andRoute(POST("/person").and(contentType(APPLICATION_JSON)), personHandler::save);
    }

    @Bean
    HttpHandler webHandler(RouterFunction<ServerResponse> routerFunction) {
        return RouterFunctions.toHttpHandler(routerFunction);
    }

    @Bean
    ReactiveWebServerFactory reactiveWebServerFactory(Environment env) {
        return new NettyReactiveWebServerFactory(env.getRequiredProperty("server.port", Integer.class));
    }
}