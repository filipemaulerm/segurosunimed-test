package com.example.api.repository;

import com.example.api.domain.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository interface for accessing and managing {@link Customer} entities in the database.
 * Extends the Spring Data CrudRepository to provide basic CRUD operations.
 */
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    /**
     * Retrieves all customers from the database, ordered by name, and paginated.
     *
     * @param pageable Pageable object specifying the page size, page number, and sorting information.
     * @return Page containing a list of customers ordered by name.
     */
    Page<Customer> findAllByOrderByNameAsc(Pageable pageable);

}
