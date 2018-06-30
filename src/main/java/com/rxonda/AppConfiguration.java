package com.rxonda;

import java.util.Properties;
import java.util.concurrent.Executors;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.rxonda.repo.PersonRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.http.MediaType.*;

@Configuration
@EnableJpaRepositories(basePackageClasses=PersonRepository.class)
public class AppConfiguration {

    @Bean
    Scheduler jdbcScheduler(@Value("${jdbc-connection-pool-size}") Integer connectionPoolSize) {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }

    @Bean
    PersonHandler personHandler(PersonRepository personRepository, Scheduler jdbcScheduler) {
        return new PersonHandler(personRepository, jdbcScheduler);
    }

    @Bean
    HttpHandler webHandler(PersonHandler personHandler) {
        RouterFunction<ServerResponse> router = RouterFunctions
            .route(GET("/person").and(accept(APPLICATION_JSON)), personHandler::list)
            .andRoute(GET("/person/{id}").and(accept(APPLICATION_JSON)), personHandler::show)
            .andRoute(POST("/person").and(contentType(APPLICATION_JSON)), personHandler::save);

        return RouterFunctions.toHttpHandler(router);
    }

    @Bean(destroyMethod = "close")
    DataSource dataSource(Environment env) {
        HikariConfig dataSourceConfig = new HikariConfig();
        dataSourceConfig.setDriverClassName(env.getRequiredProperty("db.driver"));
        dataSourceConfig.setJdbcUrl(env.getRequiredProperty("db.url"));
        dataSourceConfig.setUsername(env.getRequiredProperty("db.username"));
        dataSourceConfig.setPassword(env.getRequiredProperty("db.password"));
 
        return new HikariDataSource(dataSourceConfig);
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Environment env) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPackagesToScan("com.rxonda.domain");
 
        Properties jpaProperties = new Properties();

        jpaProperties.put("hibernate.dialect", env.getRequiredProperty("hibernate.dialect"));
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getRequiredProperty("hibernate.hbm2ddl.auto"));
        jpaProperties.put("hibernate.ejb.naming_strategy", env.getRequiredProperty("hibernate.ejb.naming_strategy"));
        jpaProperties.put("hibernate.show_sql", env.getRequiredProperty("hibernate.show_sql"));
        jpaProperties.put("hibernate.format_sql", env.getRequiredProperty("hibernate.format_sql"));
 
        entityManagerFactoryBean.setJpaProperties(jpaProperties);
 
        return entityManagerFactoryBean;
    }

    @Bean
    JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}