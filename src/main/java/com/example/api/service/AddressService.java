package com.example.api.service;

import com.example.api.domain.entity.Address;
import com.example.api.domain.entity.Customer;
import com.example.api.domain.model.AddressDto;
import com.example.api.domain.model.CustomerDto;
import com.example.api.domain.model.CustomerFilter;
import com.example.api.domain.model.ViaCepResponse;
import com.example.api.exception.BusinessException;
import com.example.api.exception.CustomerNotFound;
import com.example.api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final RestTemplate restTemplate;
    private final EntityManager entityManager;
    @Value("${via_cep.url}")
    private String viaCepUrl;

    public Page<CustomerDto> findAll(Pageable pageable) {
        return repository.findAllByOrderByNameAsc(pageable).map(this::convertTo);
    }

    public CustomerDto findById(Long id, CustomerFilter customerFilter) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Customer> query = criteriaBuilder.createQuery(Customer.class);
        Root<Customer> root = query.from(Customer.class);

        Predicate basePredicate = criteriaBuilder.equal(root.get("id"), id);

        Predicate[] additionalPredicate = Optional.ofNullable(customerFilter)
                .map(filter -> Stream.of(
                        buildEqualPredicate(criteriaBuilder, root, "name", filter.getName()),
                        buildEqualPredicate(criteriaBuilder, root, "email", filter.getEmail()),
                        buildEqualPredicate(criteriaBuilder, root, "gender", filter.getGender())
                ).filter(Objects::nonNull).toArray(Predicate[]::new))
                .orElse(null);

        Predicate finalPredicate = additionalPredicate != null
                ? criteriaBuilder.and(basePredicate, criteriaBuilder.and(additionalPredicate))
                : basePredicate;

        query.select(root).where(finalPredicate);

        return Optional.of(entityManager.createQuery(query).getSingleResult()).map(this::convertTo).orElse(null);

    }

    private Predicate buildEqualPredicate(CriteriaBuilder criteriaBuilder, Root<Customer> root, String fieldName, String value) {
        return Optional.ofNullable(value)
                .map(val -> criteriaBuilder.equal(root.get(fieldName), val))
                .orElse(null);
    }

    private CustomerDto convertTo(Customer customer) {
        return CustomerDto.builder()
                .name(customer.getName())
                .email(customer.getEmail())
                .gender(customer.getGender())
                .id(customer.getId())
                .addresses(customer.getAddresses().stream().map(a -> AddressDto.builder()
                        .id(a.getId())
                        .city(a.getCity())
                        .zipCode(a.getZipCode())
                        .street(a.getStreet())
                        .state(a.getState())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    private Customer convertTo(CustomerDto customerDto) {
        Customer customer = new Customer();
        customer.setId(customerDto.getId());
        customer.setGender(customerDto.getGender());
        customer.setEmail(customerDto.getEmail());
        customer.setName(customerDto.getName());
        customer.setAddresses(customerDto.getAddresses().stream().map(a -> {
                    Address address = new Address();
                    address.setId(a.getId());
                    address.setCity(a.getCity());
                    address.setStreet(a.getStreet());
                    address.setZipCode(a.getZipCode());
                    address.setState(a.getState());
                    address.setCustomer(customer);
                    return address;
                }
        ).collect(Collectors.toList()));
        return customer;
    }

    public CustomerDto createCustomer(CustomerDto customerDto) {
        Customer customer = convertTo(customerDto);
        customer.setId(null);
        customer.getAddresses().forEach(a -> a.setId(null));
        List<Address> addresses = customerDto.getAddresses().stream()
                .map(a -> {
                    ViaCepResponse addressByZipCode = getAddressByZipCode(a.getZipCode());
                    Address address = new Address();
                    address.setCustomer(customer);
                    address.setCity(addressByZipCode.getLocalidade());
                    address.setStreet(addressByZipCode.getLogradouro());
                    address.setState(addressByZipCode.getUf());
                    address.setZipCode(addressByZipCode.getCep());
                    return address;
                })
                .collect(Collectors.toList());
        customer.setAddresses(addresses);
        Customer savedCustomer = repository.save(customer);
        return convertTo(savedCustomer);
    }

    private ViaCepResponse getAddressByZipCode(String zipCode) {
        try {
            log.info("Getting address by zipCode {}", zipCode);
            ResponseEntity<ViaCepResponse> responseEntity = restTemplate.getForEntity(viaCepUrl+"/ws/"+zipCode+"/json", ViaCepResponse.class);
            ViaCepResponse body = responseEntity.getBody();
            return Optional.ofNullable(body)
                    .filter(viaCepResponse -> Objects.nonNull(viaCepResponse.getCep()))
                    .orElseThrow(() -> new BusinessException("The zipCode " + zipCode + " is not valid", HttpStatus.BAD_REQUEST));
        } catch (HttpClientErrorException exception) {
            String error = exception.getResponseBodyAsString();
            throw new BusinessException(error, HttpStatus.BAD_REQUEST);
        } catch (HttpServerErrorException exception) {
            String error = exception.getResponseBodyAsString();
            throw new BusinessException(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        customerDto.setId(id);
        return convertTo(repository.findById(id)
                .map(existingCustomer -> repository.save(convertTo(customerDto)))
                .orElseThrow(() -> new CustomerNotFound("The system cannot update a client that does not exist")));
    }

    @Transactional
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
