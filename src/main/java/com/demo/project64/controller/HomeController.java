package com.demo.project64.controller;

import java.util.List;
import java.util.Optional;

import com.demo.project64.domain.Customer;
import com.demo.project64.service.CustomerReactiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final CustomerReactiveService customerReactiveService;

    @GetMapping("/all")
    public Flux<Customer> findAll() {
        return customerReactiveService.findAll();
    }

    @GetMapping("/id/{customerId}")
    public Mono<Optional<Customer>> findById(@PathVariable Long customerId) {
        return customerReactiveService.findById(customerId);
    }

    @PostMapping(value = "/save")
    public Mono<Customer> save(@RequestBody Customer customer) {
        return customerReactiveService.save(customer);
    }

    @GetMapping("/find")
    public Mono<List<Customer>> findById(@RequestParam String name, @RequestParam Integer age) {
        return customerReactiveService.findByNameAndAge(name, age);
    }
}
