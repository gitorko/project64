package com.demo.reactdb.project64;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@Slf4j
public class Application implements ApplicationRunner {

    @Autowired
    AppService appService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Seeding data!");

        Flux<String> names = Flux.just("raj", "david", "pam").delayElements(Duration.ofSeconds(1));
        Flux<Integer> ages = Flux.just(25, 27, 30).delayElements(Duration.ofSeconds(1));
        Flux<Customer> customers = Flux.zip(names, ages).map(tupple -> {
            return new Customer(null, tupple.getT1(), tupple.getT2());
        });

        appService.deleteAll().thenMany(customers.flatMap(c -> appService.save(c))
                .thenMany(appService.findAll())).subscribe(System.out::println);
    }

}

@Configuration
class DataConfig {

    @Value("${spring.datasource.maximum-pool-size:100}")
    private int connectionPoolSize;

    @Bean
    public Scheduler jdbcScheduler() {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}

@Service
class AppService {

    @Autowired
    @Qualifier("jdbcScheduler")
    Scheduler jdbcScheduler;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    AddressRepository repo;

    public Flux<Customer> findAll() {
        return asyncIterable(() -> repo.findAll().iterator());
    }

    public Mono<Optional<Customer>> findById(Long id) {
        return asyncCallable(() -> repo.findById(id));
    }

    public Mono<Customer> save(Customer customer) {
        return asyncCallable(() -> repo.save(customer));
    }

    public Mono<Void> delete(Customer customer) {
        return asyncCallable(() -> {
            repo.delete(customer);
            return null;
        });
    }

    public Mono<Void> deleteAll() {
        return asyncCallable(() -> {
            repo.deleteAll();
            return null;
        });
    }

    protected <S> Mono<S> asyncCallable(Callable<S> callable) {
        return Mono.fromCallable(callable).subscribeOn(Schedulers.parallel()).publishOn(jdbcScheduler);
    }

    protected <S> Flux<S> asyncIterable(Iterable<S> iterable) {
        return Flux.fromIterable(iterable).subscribeOn(Schedulers.parallel()).publishOn(jdbcScheduler);
    }
}

@RestController
@RequestMapping("/api")
class AppController {

    @Autowired
    AppService appService;

    @GetMapping("/all")
    public Flux<Customer> findAll() {
        return appService.findAll();
    }

    @GetMapping("/id/{customerId}")
    public Mono<Optional<Customer>> findById(@PathVariable Long customerId) {
        return appService.findById(customerId);
    }

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Mono<Customer> save(@RequestBody Customer customer) {
        return appService.save(customer);
    }
}

interface AddressRepository extends CrudRepository<Customer, Long> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "customer")
class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private Integer age;
}