package com.demo.project64.repositoryservice;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public abstract class AbstractReactiveRepoService<T> {

    @Qualifier("jdbcScheduler")
    @Autowired
    Scheduler jdbcScheduler;

    public Mono<Page<T>> findAll(Pageable pageable) {
        return asyncCallable(() -> getRepository().findAll(pageable));
    }

    public Mono<Page<T>> findAllBlocking(Pageable pageable) {
        return Mono.just(getRepository().findAll(pageable));
    }

    public Flux<T> findAll() {
        return asyncIterable(() -> getRepository().findAll().iterator());
    }

    public Mono<Optional<T>> findById(Long id) {
        return asyncCallable(() -> getRepository().findById(id));
    }

    public Mono<T> save(T customer) {
        return (Mono<T>) asyncCallable(() -> getRepository().save(customer));
    }

    public Mono<Void> delete(T object) {
        return asyncCallable(() -> {
            getRepository().delete(object);
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
        return Mono.fromCallable(callable).subscribeOn(Schedulers.newParallel("jdbc-thread")).publishOn(jdbcScheduler);
    }

    protected <T> Flux<T> asyncIterable(Iterable<T> iterable) {
        return Flux.fromIterable(iterable).subscribeOn(Schedulers.newParallel("jdbc-thread")).publishOn(jdbcScheduler);
    }

    protected abstract JpaRepository getRepository();

}
