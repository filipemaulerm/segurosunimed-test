package com.example.api.web.rest;

import com.example.api.domain.model.AddressDto;
import com.example.api.service.AddressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @Mock
    private AddressService service;
    @InjectMocks
    private AddressController addressController;

    @Test
    void testFindAll() {
        Pageable pageable = Page.empty().getPageable();
        when(service.findAll(any())).thenReturn(new PageImpl<>(Collections.singletonList(new AddressDto()),
                pageable, 1));

        Page<AddressDto> page = addressController.findAll(pageable);

        assertEquals(1, page.getSize());
    }
}