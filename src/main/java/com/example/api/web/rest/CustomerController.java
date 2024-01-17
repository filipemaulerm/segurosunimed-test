package com.example.api.web.rest;

import com.example.api.domain.model.CustomerDto;
import com.example.api.domain.model.CustomerFilter;
import com.example.api.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST Controller for managing customer-related operations.
 */
@Slf4j
@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    /**
     * Retrieves all customers with pagination.
     *
     * @param pageable Pageable object specifying the page size, page number, and sorting information.
     * @return Page containing a list of customers ordered by name.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<CustomerDto> findAll(Pageable pageable,  @Valid @ModelAttribute CustomerFilter customerFilter) {
        log.info("Fetching all customers with pagination.");
        return service.findAll(pageable, customerFilter);
    }

    /**
     * Retrieves a customer by ID and applies optional filtering based on customer information.
     *
     * @param id             ID of the customer to retrieve.
     * @param customerFilter Filter criteria for customer information.
     * @return CustomerDto representing the retrieved customer.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto findById(@PathVariable Long id, @Valid @ModelAttribute CustomerFilter customerFilter) {
        log.info("Fetching customer by ID: {}", id);
        return service.findById(id, customerFilter);
    }

    /**
     * Creates a new customer based on the provided DTO.
     *
     * @param customerDto DTO representing the customer information.
     * @return CustomerDto representing the created customer.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        log.info("Creating a new customer: {}", customerDto);
        return service.createCustomer(customerDto);
    }

    /**
     * Updates an existing customer based on the provided ID and DTO.
     *
     * @param id          ID of the customer to update.
     * @param customerDto DTO representing the updated customer information.
     * @return CustomerDto representing the updated customer.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDto customerDto) {
        log.info("Updating customer with ID {}: {}", id, customerDto);
        return service.updateCustomer(id, customerDto);
    }

    /**
     * Deletes an existing customer based on the provided ID.
     *
     * @param id ID of the customer to delete.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {
        log.info("Deleting customer with ID: {}", id);
        service.deleteCustomer(id);
    }
}
