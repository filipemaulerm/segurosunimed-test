package com.example.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class CustomerDto {

    private Long id;
    @NotBlank
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    @Email(message = "Must be valid like email@domain.com")
    private String email;
    @NotBlank
    @Pattern(regexp = "^(M|F)$", message = "Gender must be either 'M' or 'F'")
    private String gender;
    @Valid
    @NotEmpty(message = "Customer must have at least one address")
    private List<AddressDto> addresses;
}
