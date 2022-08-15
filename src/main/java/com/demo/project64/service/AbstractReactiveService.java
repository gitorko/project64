package com.demo.project64.service;

import java.util.Optional;
import java.util.concurrent.Callable;

import com.demo.project64.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public abstract class AbstractReactiveService<T> {

    @Qualifier("jdbcScheduler")
    @Autowired
    Scheduler jdbcScheduler;

    public Flux<T> findAll() {
        return asyncIterable(() -> getRepository().findAll().iterator());
    }

    public Mono<Optional<T>> findById(Long id) {
        return asyncCallable(() -> getRepository().findById(id));
    }

    public Mono<T> save(Customer customer) {
        return (Mono<T>) asyncCallable(() -> getRepository().save(customer));
    }

    public Mono<Void> delete(Customer customer) {
        return asyncCallable(() -> {
            getRepository().delete(customer);
            return null;
        });
    }

    public Mono<Void> deleteAll() {
        return asyncCallable(() -> {
            getRepository().deleteAll();
            return null;
        });
    }

    protected <T> Mono<T> asyncCallable(Callable<T> callable) {
        return Mono.fromCallable(callable).subscribeOn(Schedulers.parallel()).publishOn(jdbcScheduler);
    }

    protected <T> Flux<T> asyncIterable(Iterable<T> iterable) {
        return Flux.fromIterable(iterable).subscribeOn(Schedulers.parallel()).publishOn(jdbcScheduler);
    }

    protected abstract CrudRepository getRepository();
}
