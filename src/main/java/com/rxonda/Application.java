package com.rxonda;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.rxonda.domain.Person;
import com.rxonda.repo.PersonRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

/*
 * @author Raphael R. costa
 */
public class Application {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException {
        LOGGER.info("Initializing Application");

        ApplicationContext applicationContext = new SpringApplicationBuilder()
            .sources(WebConfiguration.class, PersistencyConfiguration.class)
            .bannerMode(Mode.OFF)
            .web(WebApplicationType.REACTIVE)
            .build(args)
            .run(args);

        initializingData(applicationContext.getBean(PersonRepository.class));

    }

    public static void initializingData(PersonRepository personRepository) {
        LOGGER.info("Loading data...");

        personRepository.save(new Person("Raphael Rodrigues", "Rua das Couves 320"));
        personRepository.save(new Person("Claudio Rodrigues", "Rua das Maçãs 120"));
        personRepository.save(new Person("Maurila Rodrigues", "Rua das Laranjas 96"));
        personRepository.save(new Person("Marcia Luiza", "Rua das Flores 32"));
        personRepository.flush();

        LOGGER.info("Finish Loading data");
    }
}
