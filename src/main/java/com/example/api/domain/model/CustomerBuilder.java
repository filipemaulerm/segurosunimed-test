package com.example.api.domain.model;

import com.example.api.domain.entity.Address;
import com.example.api.domain.entity.Customer;

import java.util.stream.Collectors;

public class CustomerBuilder {

    private CustomerBuilder(){

    }
    public static CustomerDto convertTo(Customer customer) {
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

    public static Customer convertTo(CustomerDto customerDto) {
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
}
