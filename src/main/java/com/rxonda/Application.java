package com.rxonda;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.rxonda.domain.Person;
import com.rxonda.repo.PersonRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import reactor.ipc.netty.http.server.HttpServer;

/*
 * @author Raphael R. costa
 */
public class Application {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException {
        ConfigurableEnvironment environment = getEnvironment();

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(AppConfiguration.class);
        applicationContext.setEnvironment(environment);
        applicationContext.refresh();

        LOGGER.info("Loading data...");

        PersonRepository personRepository = applicationContext.getBean(PersonRepository.class);
        personRepository.save(new Person("Raphael Rodrigues", "Rua das Couves 320"));
        personRepository.save(new Person("Claudio Rodrigues", "Rua das Maçãs 120"));
        personRepository.save(new Person("Maurila Rodrigues", "Rua das Laranjas 96"));
        personRepository.save(new Person("Marcia Luiza", "Rua das Flores 32"));
        personRepository.flush();

        LOGGER.info("Finish Loading data. Starting server...");

        int port = Integer.valueOf(environment.getProperty("server.port", "8080"));

        HttpServer httpServer = HttpServer.create("localhost", port);

        httpServer.newHandler(new ReactorHttpHandlerAdapter(getAppHandler(applicationContext))).block();

        LOGGER.info(String.format("Server started at port %d", port));

        while(true) {
            if(Thread.currentThread().isInterrupted()) {
                break;
            }
        }
        System.out.println("Application signaling to interrupt!");
        System.exit(1);
    }

    public static HttpHandler getAppHandler(ApplicationContext applicationContext) {
		return WebHttpHandlerBuilder.applicationContext(applicationContext)
            .build();
    }

    public static ConfigurableEnvironment getEnvironment() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        try {
            Properties properties = new Properties();
            properties.load(Application.class.getClassLoader()
                .getResourceAsStream("application.properties"));
            Map myMap = new HashMap<>();
            for(Entry<Object, Object> entry : properties.entrySet()) {
                myMap.put(entry.getKey(), entry.getValue());
            }
            propertySources.addFirst(new MapPropertySource("MY_MAP", myMap));
        } catch(IOException ioe) {
            LOGGER.error("application.properties file NOT FOUND!", ioe);
        }
        return environment;
    }
}
