package com.grab.api.controller.dto;

import com.grab.api.service.domain.driver.Driver;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request payload to create a new driver")
public record DriverCreateDTO(
    @Schema(description = "Full name of the driver", example = "John Doe") @NotBlank
    String fullName,

    @Schema(
        description = "Mobile phone number of the driver in E.164 format",
        example = "+6591234567")
    @NotBlank
    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "must be a valid E.164 phone number (e.g., +6591234567)")
    String mobilePhone,

    @Schema(description = "Age of the driver", example = "30") @NotNull @Min(18) @Max(100)
    Integer age,

    @Schema(description = "Rating of the driver", example = "4.5")
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    Double rating,

    @Schema(description = "Whether the driver is verified", example = "false") @NotNull
    Boolean isVerified,

    @Schema(description = "Balance of the driver", example = "1000.50") @NotNull
    BigDecimal balance,

    @Schema(description = "Date of birth of the driver", example = "1990-01-15") @NotNull
    LocalDate dateOfBirth) {

  public Driver driver() {
    return Driver.newDriver(fullName, mobilePhone, age, rating, isVerified, balance, dateOfBirth);
  }
}
