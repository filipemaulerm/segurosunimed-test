package com.example.api.service;

import com.example.api.domain.entity.Customer;
import com.example.api.domain.model.AddressDto;
import com.example.api.domain.model.CustomerDto;
import com.example.api.domain.model.CustomerFilter;
import com.example.api.domain.model.ViaCepResponse;
import com.example.api.exception.CustomerNotFound;
import com.example.api.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void findById() {
        Long customerId = 1L;
        CustomerFilter customerFilter = CustomerFilter.builder().build();
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<Customer> query = mock(CriteriaQuery.class);
        Root<Customer> root = mock(Root.class);
        TypedQuery<Customer> typedQuery = mock(TypedQuery.class);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Customer.class)).thenReturn(query);
        when(query.from(Customer.class)).thenReturn(root);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(mock(Customer.class));

        when(query.select(any())).thenReturn(query);

        CustomerDto result = customerService.findById(customerId, customerFilter);

        assertNotNull(result);
        verify(entityManager, times(2)).getCriteriaBuilder();
    }


    @Test
    void createCustomer() {
        CustomerDto customerDto = mock(CustomerDto.class);
        ViaCepResponse viaCepResponse = mock(ViaCepResponse.class);
        AddressDto address = AddressDto.builder()
                .zipCode("650644658")
                .build();
        when(customerDto.getAddresses()).thenReturn(Collections.singletonList(address));
        when(addressService.getAddressByZipCode(anyString())).thenReturn(viaCepResponse);
        when(repository.save(any())).thenReturn(mock(Customer.class));

        CustomerDto result = customerService.createCustomer(customerDto);

        assertNotNull(result);
        verify(repository, times(1)).save(any());
    }

    @Test
    void updateCustomer() {
        Long customerId = 1L;
        CustomerDto customerDto = mock(CustomerDto.class);
        when(repository.findById(customerId)).thenReturn(Optional.of(mock(Customer.class)));
        when(repository.save(any())).thenReturn(mock(Customer.class));

        CustomerDto result = customerService.updateCustomer(customerId, customerDto);

        assertNotNull(result);
        verify(repository, times(1)).findById(customerId);
        verify(repository, times(1)).save(any());
    }

    @Test
    void deleteCustomer() {
        Long customerId = 1L;
        when(repository.findById(customerId)).thenReturn(Optional.of(mock(Customer.class)));

        assertDoesNotThrow(() -> customerService.deleteCustomer(customerId));

        verify(repository, times(1)).findById(customerId);
        verify(repository, times(1)).deleteById(customerId);
    }

    @Test
    void deleteCustomerNotFound() {
        Long customerId = 1L;
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFound.class, () -> customerService.deleteCustomer(customerId));

        verify(repository, times(1)).findById(customerId);
        verify(repository, never()).deleteById(customerId);
    }
}
