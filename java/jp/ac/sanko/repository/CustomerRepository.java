package jp.ac.sanko.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.ac.sanko.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    Customer findByEmail(String email);
}