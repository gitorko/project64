package com.demo.project64.repository;

import java.util.List;

import com.demo.project64.domain.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    List<Customer> findByNameAndAge(String name, Integer age);
}
