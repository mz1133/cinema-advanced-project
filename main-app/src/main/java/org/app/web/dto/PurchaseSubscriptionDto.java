package org.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseSubscriptionDto {

    @NotBlank(message = "Please select a plan.")
    private String planCode;

    @NotBlank(message = "Card number is required.")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must contain exactly 16 digits.")
    private String cardNumber;

    @NotBlank(message = "Expiry date is required.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/([0-9]{2})$", message = "Invalid format (MM/YY). Example: 12/28")
    private String expiry;

    @NotBlank(message = "CVV is required.")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must contain 3 or 4 digits.")
    private String cvv;
}
