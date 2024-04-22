package com.demo.project64;

import java.time.Duration;

import com.demo.project64.domain.Customer;
import com.demo.project64.repositoryservice.CustomerReactiveRepoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Slf4j
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner seedData(CustomerReactiveRepoService customerReactiveService) {
        return args -> {
            log.info("Seeding data!");

            Flux<String> names = Flux.just("raj", "david", "pam").delayElements(Duration.ofSeconds(1));
            Flux<Integer> ages = Flux.just(25, 27, 30).delayElements(Duration.ofSeconds(1));
            Flux<Customer> customers = Flux.zip(names, ages).map(tupple -> {
                return new Customer(null, tupple.getT1(), tupple.getT2());
            });

            customerReactiveService.deleteAll().thenMany(customers.flatMap(c -> customerReactiveService.save(c))
                    .thenMany(customerReactiveService.findAll())).subscribe(System.out::println);
        };
    }

}

