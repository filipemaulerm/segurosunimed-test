package com.example.api.client;

import com.example.api.domain.model.ViaCepResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "viaCepService", url = "${via_cep.url}")
public interface ViaCepClient {

    @GetMapping(value = "/ws/{zipCode}/json")
    ViaCepResponse getAddressByZipCode(@PathVariable("zipCode") String zipCode);

}