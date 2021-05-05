package com.example.api.model.dto;

import lombok.Data;

@Data
public class RegisterUserDto {

    private String email;

    private String password;

    private String code;

}
