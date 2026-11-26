package com.dsantillan.tfm.patients.model.dto;

import com.dsantillan.tfm.common.identification.model.enums.IdentificationType;
import com.dsantillan.tfm.common.model.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponseDto {

    @NotBlank(message = "Id is required")
    private long patientId;

    @NotBlank(message = "Document number is required")
    @Size(max = 20, message = "Document number cannot exceed 20 characters")
    private String documentNumber;

    @NotNull(message = "Document type is required")
    private IdentificationType documentType;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be before the current date")
    private LocalDate birthDate;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @JsonIgnore
    private long version; // Add the version field for optimistic locking
}
