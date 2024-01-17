package com.example.api.web.rest;

import com.example.api.domain.model.CustomerDto;
import com.example.api.domain.model.CustomerFilter;
import com.example.api.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @Test
    void findAll() {
        Page<CustomerDto> customerDtoPage = Page.empty();
        when(customerService.findAll(any(Pageable.class), any(CustomerFilter.class))).thenReturn(customerDtoPage);

        Page<CustomerDto> result = customerController.findAll(Page.empty().getPageable(), CustomerFilter.builder().build());

        assertEquals(customerDtoPage, result);
        verify(customerService, times(1)).findAll(any(Pageable.class),  any(CustomerFilter.class));
    }

    @Test
    void findById() {
        CustomerDto customerDto = new CustomerDto();
        Long customerId = 1L;
        when(customerService.findById(eq(customerId), any(CustomerFilter.class))).thenReturn(customerDto);

        CustomerDto result = customerController.findById(customerId, mock(CustomerFilter.class));

        assertEquals(customerDto, result);
        verify(customerService, times(1)).findById(eq(customerId), any(CustomerFilter.class));
    }

    @Test
    void createCustomer() {
        CustomerDto customerDto = new CustomerDto();
        when(customerService.createCustomer(any(CustomerDto.class))).thenReturn(customerDto);

        CustomerDto result = customerController.createCustomer(mock(CustomerDto.class));

        assertEquals(customerDto, result);
        verify(customerService, times(1)).createCustomer(any(CustomerDto.class));
    }

    @Test
    void updateCustomer() {
        CustomerDto customerDto = new CustomerDto();
        Long customerId = 1L;
        when(customerService.updateCustomer(eq(customerId), any(CustomerDto.class))).thenReturn(customerDto);

        CustomerDto result = customerController.updateCustomer(customerId, mock(CustomerDto.class));

        assertEquals(customerDto, result);
        verify(customerService, times(1)).updateCustomer(eq(customerId), any(CustomerDto.class));
    }

    @Test
    void deleteCustomer() {
        Long customerId = 1L;
        doNothing().when(customerService).deleteCustomer(customerId);

        customerController.deleteCustomer(customerId);

        verify(customerService, times(1)).deleteCustomer(customerId);
    }
}