package com.rxonda.handler;

import com.rxonda.domain.Person;
import com.rxonda.repo.PersonRepository;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

public class PersonHandler {

    private final PersonRepository personRepository;

    private final Scheduler scheduler;

    public PersonHandler(PersonRepository personRepository, Scheduler scheduler) {
        this.personRepository = personRepository;
        this.scheduler = scheduler;
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        Flux<Person> people = Flux.defer(() -> Flux.fromIterable(this.personRepository.findAll())).subscribeOn(scheduler);
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(people, Person.class);
    }
    
    public Mono<ServerResponse> show(ServerRequest request) {
        Long personId = Long.valueOf(request.pathVariable("id"));
        return Mono.fromCallable(() -> this.personRepository.findById(personId).orElse(new Person()))
            .publishOn(scheduler)
            .flatMap(person -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromObject(person)))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        return request
            .bodyToMono(Person.class)
            .flatMap(person -> Mono.fromCallable(() -> this.personRepository.save(person)))
            .publishOn(scheduler)
            .flatMap(person -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromObject(person)));
    }
}