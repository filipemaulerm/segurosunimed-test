package com.example.api.service;

import com.example.api.domain.entity.Address;
import com.example.api.domain.model.AddressDto;
import com.example.api.domain.model.ViaCepResponse;
import com.example.api.exception.BusinessException;
import com.example.api.repository.AddressRepository;
import com.example.api.service.feignclient.ViaCepClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {
    @Mock
    private ViaCepClient viaCepClient;
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @Test
    void testGetAddressByZipCodeValidZipCode() {
        // Mock the successful response from the external service
        ViaCepResponse viaCepResponse = new ViaCepResponse();
        viaCepResponse.setCep("12345-678");
        viaCepResponse.setLocalidade("City");
        viaCepResponse.setLogradouro("Street");
        viaCepResponse.setUf("State");

        when(viaCepClient.getAddressByZipCode(anyString())).thenReturn(viaCepResponse);

        // Test the method with a valid zip code
        ViaCepResponse result = addressService.getAddressByZipCode("12345678");

        // Verify that the result matches the mocked response
        assertNotNull(result);
        assertEquals("12345-678", result.getCep());
        assertEquals("City", result.getLocalidade());
        assertEquals("Street", result.getLogradouro());
        assertEquals("State", result.getUf());
    }

    @Test
    void testGetAddressByZipCodeInvalidZipCode() {
        // Mock the response from the external service with an invalid zip code
        when(viaCepClient.getAddressByZipCode(anyString())).thenReturn(new ViaCepResponse());

        // Test the method with an invalid zip code
        BusinessException exception = assertThrows(BusinessException.class,
                () -> addressService.getAddressByZipCode("invalidZipCode"));

        // Verify that the exception has the expected details
        assertEquals("The zipCode invalidZipCode is not valid", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testGetAddressByZipCodeClientError() {
        // Mock a client-side error from the external service
        when(viaCepClient.getAddressByZipCode(anyString())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Client Error"));

        // Test the method with a client-side error
        BusinessException exception = assertThrows(BusinessException.class,
                () -> addressService.getAddressByZipCode("12345678"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void testGetAddressByZipCodeServerError() {
        // Mock a server-side error from the external service
        when(viaCepClient.getAddressByZipCode(anyString())).thenThrow(
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        // Test the method with a server-side error
        BusinessException exception = assertThrows(BusinessException.class,
                () -> addressService.getAddressByZipCode("12345678"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void testFindAll() {
        Pageable pageable = Page.empty().getPageable();
        when(addressRepository.findAllByOrderByCityAsc(any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(new Address()), pageable, 1));

        Page<AddressDto> page = addressService.findAll(pageable);

        assertEquals(1, page.getSize());
    }
}