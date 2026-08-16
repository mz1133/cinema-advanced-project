package org.app.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddActorDto {

    @NotBlank(message = "First name cant be empty.")
    private String firstName;

    @NotBlank(message = "Last name cant be empty.")
    private String lastName;

    @NotNull(message = "Birth date cant be empty.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String pictureUrl;

    @NotNull(message = "Age cannot be empty.")
    @Min(value = 0, message = "Age can't be less than 0 year.")
    @Max(value = 130, message = "Age can't be high than 130 year.")
    private Integer age;

    private String biography;


}
