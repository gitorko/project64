package com.demo.project64.service;

import java.util.List;

import com.demo.project64.domain.Customer;
import com.demo.project64.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerReactiveService extends AbstractReactiveService<Customer> {

    private final CustomerRepository repository;

    public Mono<List<Customer>> findByNameAndAge(String name, Integer age) {
        return asyncCallable(() -> repository.findByNameAndAge(name, age));
    }

    @Override
    protected CrudRepository getRepository() {
        return repository;
    }
}
