package com.example.api.domain;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;

@Builder
@ToString
@Getter
@Setter
public class CustomerFilter {
    private String name;
    @Email(message = "Please provide a valid email address")
    private String email;
    private String gender;
}