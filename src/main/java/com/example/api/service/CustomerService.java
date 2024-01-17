package com.example.api.service;

import com.example.api.domain.entity.Address;
import com.example.api.domain.entity.Customer;
import com.example.api.domain.model.AddressDto;
import com.example.api.domain.model.CustomerBuilder;
import com.example.api.domain.model.CustomerDto;
import com.example.api.domain.model.CustomerFilter;
import com.example.api.domain.model.ViaCepResponse;
import com.example.api.exception.CustomerNotFound;
import com.example.api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.api.domain.model.CustomerBuilder.convertTo;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final EntityManager entityManager;
    private final AddressService addressService;

    /**
     * Retrieves all customers with pagination.
     *
     * @param pageable Pageable object specifying the page size, page number, and sorting information.
     * @return Page containing a list of customers ordered by name.
     */
    public Page<CustomerDto> findAll(Pageable pageable, CustomerFilter customerFilter) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Customer> query = criteriaBuilder.createQuery(Customer.class);

        buildCriteria(customerFilter, query);

        List<CustomerDto> customerDtos = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList()
                .stream()
                .map(CustomerBuilder::convertTo)
                .collect(Collectors.toList());

        CriteriaQuery<Customer> countQuery = criteriaBuilder.createQuery(Customer.class);
        countQuery.select(countQuery.from(Customer.class));
        int totalCount = entityManager.createQuery(countQuery).getResultList().size();
        return new PageImpl<>(customerDtos, pageable, totalCount);

    }

    private void buildCriteria(CustomerFilter customerFilter, CriteriaQuery<Customer> query) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        Root<Customer> root = query.from(Customer.class);
        Join<Customer, Address> addressJoin = root.join("addresses", JoinType.LEFT);

        Predicate[] additionalPredicate = Optional.ofNullable(customerFilter)
                .map(filter -> Stream.of(
                        buildEqualPredicate(criteriaBuilder, root, "name", filter.getName()),
                        buildEqualPredicate(criteriaBuilder, root, "email", filter.getEmail()),
                        buildEqualPredicate(criteriaBuilder, root, "gender", filter.getGender()),
                        buildEqualPredicate(criteriaBuilder, addressJoin, "state", filter.getState()),
                        buildEqualPredicate(criteriaBuilder, addressJoin, "city", filter.getCity())
                ).filter(Objects::nonNull).toArray(Predicate[]::new))
                .orElse(null);

        Predicate finalPredicate = criteriaBuilder.conjunction();
        if (customerFilter.getId() != null) {
            Predicate basePredicate = criteriaBuilder.equal(root.get("id"), customerFilter.getId());
            finalPredicate = criteriaBuilder.and(basePredicate, criteriaBuilder.and(additionalPredicate));
        } else if (additionalPredicate.length > 0){
            finalPredicate = criteriaBuilder.and(additionalPredicate);
        }

        query.select(root).where(finalPredicate);

    }

    /**
     * Retrieves a customer by ID and applies optional filtering based on customer information.
     *
     * @param id             ID of the customer to retrieve.
     * @param customerFilter Filter criteria for customer information.
     * @return CustomerDto representing the retrieved customer.
     */
    public CustomerDto findById(Long id, CustomerFilter customerFilter) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Customer> query = criteriaBuilder.createQuery(Customer.class);
        buildCriteria(customerFilter, query);
        return Optional.of(entityManager.createQuery(query).getSingleResult()).map(CustomerBuilder::convertTo).orElse(null);
    }

    /**
     * Builds a predicate for equal comparison based on the field name and value.
     *
     * @param criteriaBuilder CriteriaBuilder for building JPA criteria.
     * @param root            Root representing the entity.
     * @param fieldName       Name of the field for comparison.
     * @param value           Value to compare against.
     * @return Predicate for equal comparison.
     */
    private Predicate buildEqualPredicate(CriteriaBuilder criteriaBuilder, Root<Customer> root, String fieldName, String value) {
        return Optional.ofNullable(value)
                .map(val -> criteriaBuilder.equal(root.get(fieldName), val))
                .orElse(null);
    }

    /**
     * Builds a predicate for equal comparison based on the field name and value within a join.
     *
     * @param criteriaBuilder CriteriaBuilder for building JPA criteria.
     * @param join            Join representing the association.
     * @param fieldName       Name of the field for comparison.
     * @param value           Value to compare against.
     * @return Predicate for equal comparison within a join.
     */
    private Predicate buildEqualPredicate(CriteriaBuilder criteriaBuilder, Join<Customer, ?> join, String fieldName, String value) {
        return Optional.ofNullable(value)
                .map(val -> criteriaBuilder.equal(join.get(fieldName), val))
                .orElse(null);
    }

    /**
     * Creates a new customer and associated addresses based on the provided DTO.
     *
     * @param customerDto DTO representing the customer information.
     * @return CustomerDto representing the created customer.
     */
    public CustomerDto createCustomer(CustomerDto customerDto) {
        Customer customer = convertTo(customerDto);
        customer.setId(null);
        customer.getAddresses().forEach(a -> a.setId(null));
        List<Address> addresses = populateAddress(customerDto, customer);
        customer.setAddresses(addresses);
        Customer savedCustomer = repository.save(customer);
        return convertTo(savedCustomer);
    }

    private List<Address> populateAddress(CustomerDto customerDto, Customer customer){
        return customerDto.getAddresses().stream()
                .map(a -> {
                    ViaCepResponse addressByZipCode = addressService.getAddressByZipCode(a.getZipCode());
                    Address address = new Address();
                    address.setCustomer(customer);
                    address.setCity(addressByZipCode.getLocalidade());
                    address.setStreet(addressByZipCode.getLogradouro());
                    address.setState(addressByZipCode.getUf());
                    address.setZipCode(addressByZipCode.getCep());
                    return address;
                })
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing customer based on the provided ID and DTO.
     *
     * @param id          ID of the customer to update.
     * @param customerDto DTO representing the updated customer information.
     * @return CustomerDto representing the updated customer.
     */
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        customerDto.setId(id);
        return convertTo(repository.findById(id)
                .map(existingCustomer -> {
                    List<Address> addresses = populateAddress(customerDto, existingCustomer);
                    customerDto.setAddresses(addresses.stream()
                            .map(a ->
                                    AddressDto.builder()
                                            .state(a.getState())
                                            .city(a.getCity())
                                            .street(a.getStreet())
                                            .zipCode(a.getZipCode())
                                            .id(a.getId())
                                            .build()
                            ).collect(Collectors.toList()));
                    return repository.save(convertTo(customerDto));
                }).orElseThrow(() -> new CustomerNotFound("The system cannot update a client that does not exist")));
    }

    /**
     * Deletes an existing customer based on the provided ID.
     *
     * @param id ID of the customer to delete.
     */
    public void deleteCustomer(Long id) {
        repository.findById(id)
                .ifPresentOrElse(
                        existingCustomer -> repository.deleteById(id),
                        () -> {
                            throw new CustomerNotFound("The system cannot delete a client that does not exist");
                        }
                );
    }
}
