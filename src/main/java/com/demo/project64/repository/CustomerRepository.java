package com.demo.project64.repository;

import java.util.List;

import com.demo.project64.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByNameAndAge(String name, Integer age);
}
