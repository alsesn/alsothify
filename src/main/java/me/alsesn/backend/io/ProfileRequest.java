package me.alsesn.backend.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileRequest {
    @NotBlank(message = "name should be not empty")
    private String name;
    @Email(message = "enter valid email address")
    @NotNull(message = "email should be not empty")
    private String email;
    @Size(min = 6, message = "password must be atleast 6 characters")
    private String password;
}
