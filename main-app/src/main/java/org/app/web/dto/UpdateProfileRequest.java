package org.app.web.dto;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @URL(message = "Please enter valid Url!")
    private String pictureUrl;

    private String firstName;

    private String lastName;

    private String email;

    @Pattern(regexp = "^$|^[0-9]{10,16}$", message = "Only number are alloyed, between 10 and 16 digits!")
    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;


}
