package com.demo.project64.repositoryservice;

import java.util.List;

import com.demo.project64.domain.Customer;
import com.demo.project64.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerReactiveRepoService extends AbstractReactiveRepoService<Customer> {

    private final CustomerRepository customerRepository;

    @Override
    protected JpaRepository getRepository() {
        return customerRepository;
    }

    public Mono<List<Customer>> findByNameAndAge(String name, Integer age) {
        return asyncCallable(() -> customerRepository.findByNameAndAge(name, age));
    }

}


