package jp.ac.sanko.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jp.ac.sanko.entity.Customer;
import jp.ac.sanko.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public boolean registerCustomer(Customer customer) {

        if (customerRepository.existsByEmail(customer.getEmail())) {
            return false;
        }

        customer.setPassword(
                passwordEncoder.encode(customer.getPassword())
        );

        customerRepository.save(customer);

        return true;
    }
}