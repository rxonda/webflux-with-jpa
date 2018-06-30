package com.rxonda;

import com.rxonda.domain.Person;
import com.rxonda.repo.PersonRepository;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PersonHandler {
    
    private PersonRepository personRepository;

    public PersonHandler(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        Flux<Person> people = Flux.fromIterable(this.personRepository.findAll());
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(people, Person.class);
    }
    
    public Mono<ServerResponse> show(ServerRequest request) {
        Long personId = Long.valueOf(request.pathVariable("id"));
        Mono<Person> personMono = Mono.just(this.personRepository.findById(personId).orElse(new Person()));
        
		return personMono
				.flatMap(person -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromObject(person)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}