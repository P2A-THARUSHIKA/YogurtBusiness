package jp.ac.sanko.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jp.ac.sanko.entity.Customer;
import jp.ac.sanko.repository.CustomerRepository;

@Service
public class CustomerDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:rithuadmin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:2056rithu}")
    private String adminPassword;

    public CustomerDetailsService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {

        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // ADMIN LOGIN
        if (adminUsername.equalsIgnoreCase(email)) {

            if (adminPassword == null || adminPassword.isBlank()) {
                throw new UsernameNotFoundException(
                        "Admin password is not configured");
            }

            return User.withUsername(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles("ADMIN")
                    .build();
        }

        // CUSTOMER LOGIN
        Customer customer = customerRepository.findByEmail(email);

        if (customer == null) {
            throw new UsernameNotFoundException("Customer not found");
        }

        return User.withUsername(customer.getEmail())
                .password(customer.getPassword())
                .roles("CUSTOMER")
                .build();
    }
}