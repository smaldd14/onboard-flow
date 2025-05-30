package com.hooswhere.onboardFlow.service;

import com.hooswhere.onboardFlow.entity.CustomerEntity;
import com.hooswhere.onboardFlow.models.CustomerRequest;
import com.hooswhere.onboardFlow.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerEntity createOrUpdateCustomer(CustomerRequest customerRequest) {
        Optional<CustomerEntity> customer = customerRepository.findByEmail(customerRequest.email());

        if (customer.isPresent()) {
            // Update existing customer
            CustomerEntity existingCustomer = customer.get();
            existingCustomer.setFirstName(customerRequest.firstName());
            existingCustomer.setLastName(customerRequest.lastName());
            existingCustomer.setCompanyName(customerRequest.companyName());
            existingCustomer.setMetadata(customerRequest.metadata());
            return customerRepository.save(existingCustomer);
        } else {
            // Create new customer
            CustomerEntity newCustomer = new CustomerEntity();
            newCustomer.setEmail(customerRequest.email());
            newCustomer.setFirstName(customerRequest.firstName());
            newCustomer.setLastName(customerRequest.lastName());
            newCustomer.setCompanyName(customerRequest.companyName());
            newCustomer.setMetadata(customerRequest.metadata());
            return customerRepository.save(newCustomer);
        }
    }
}
