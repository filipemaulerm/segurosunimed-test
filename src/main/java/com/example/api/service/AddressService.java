package com.example.api.service;

import com.example.api.domain.model.AddressDto;
import com.example.api.domain.model.ViaCepResponse;
import com.example.api.exception.BusinessException;
import com.example.api.repository.AddressRepository;
import com.example.api.service.feignclient.ViaCepClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final ViaCepClient viaCepClient;
    private final AddressRepository addressRepository;

    /**
     * Retrieves address information based on the provided zip code.
     *
     * @param zipCode Zip code to retrieve address information.
     * @return ViaCepResponse containing the address details.
     * @throws BusinessException If the zip code is not valid or if there is an error in the external service.
     */
    public ViaCepResponse getAddressByZipCode(String zipCode) {
        try {
            log.info("Getting address by zipCode {}", zipCode);
            ViaCepResponse body = viaCepClient.getAddressByZipCode(zipCode);
            // Validate the response and throw an exception if the zip code is not valid
            return Optional.ofNullable(body)
                    .filter(viaCepResponse -> Objects.nonNull(viaCepResponse.getCep()))
                    .orElseThrow(() -> new BusinessException("The zipCode " + zipCode + " is not valid", HttpStatus.BAD_REQUEST));
        } catch (FeignException.FeignClientException exception) {
            String error = exception.getMessage();
            log.error("Error occurred while fetching address information: {}", error, exception);
            HttpStatus httpStatus = HttpStatus.resolve(exception.status());
            if(httpStatus == HttpStatus.NOT_FOUND){
                throw new BusinessException(String.format("ZipCode %s is not valid", zipCode), HttpStatus.BAD_REQUEST);
            } else {
                throw new BusinessException(exception.getMessage(), httpStatus);
            }
        }
    }

    /**
     * Retrieves all addresses with pagination, ordered by city in ascending order.
     *
     * @param pageable Pageable object specifying the page size, page number, and sorting information.
     * @return Page containing a list of addresses.
     */
    public Page<AddressDto> findAll(Pageable pageable) {
        return this.addressRepository.findAllByOrderByCityAsc(pageable)
                .map(a -> AddressDto.builder()
                        .city(a.getCity())
                        .state(a.getState())
                        .street(a.getStreet())
                        .id(a.getId())
                        .zipCode(a.getZipCode())
                        .build());
    }

}
