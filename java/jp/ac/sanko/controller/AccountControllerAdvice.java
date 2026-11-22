package jp.ac.sanko.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jp.ac.sanko.entity.Customer;
import jp.ac.sanko.repository.CustomerRepository;

@ControllerAdvice
public class AccountControllerAdvice {

    private final CustomerRepository customerRepository;

    public AccountControllerAdvice(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    private Customer getLoggedInCustomer() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return customerRepository.findByEmail(auth.getName());
    }

    @ModelAttribute("accountEmail")
    public String accountEmail() {

        Customer customer = getLoggedInCustomer();

        return customer != null ? customer.getEmail() : null;
    }

    @ModelAttribute("accountInitials")
    public String accountInitials() {

        Customer customer = getLoggedInCustomer();

        if (customer == null) {
            return "G";
        }

        String[] names = customer.getFullName().trim().split("\\s+");

        String initials = "";

        for (String name : names) {
            if (!name.isEmpty()) {
                initials += name.substring(0, 1).toUpperCase();
            }

            if (initials.length() == 2) {
                break;
            }
        }

        return initials;
    }

    @ModelAttribute("accountLabel")
    public String accountLabel() {

        Customer customer = getLoggedInCustomer();

        return customer != null
                ? customer.getFullName().split("\\s+")[0]
                : "Guest";
    }
}