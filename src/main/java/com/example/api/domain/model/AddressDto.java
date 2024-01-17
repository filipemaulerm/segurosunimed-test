package com.example.api.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class AddressDto {
    private Long id;
    private String street;
    private String city;
    private String state;
    @NotBlank
    @Pattern(regexp = "^\\d{8}$", message = "Zip code must be in the format 65064589")
    private String zipCode;
}