package com.dsantillan.tfm.patients.controller;

import com.dsantillan.tfm.patients.model.dto.PatientDto;
import com.dsantillan.tfm.patients.model.dto.PatientResponseDto;
import com.dsantillan.tfm.patients.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PatientsController {

    private final PatientService patientService;

    @PostMapping("/patients")
    @PreAuthorize("hasAnyRole('dentist', 'admin')")
    public ResponseEntity<PatientResponseDto> addPatient(
            @Valid @RequestBody PatientDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID dentistId = UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
        PatientResponseDto response = patientService.createPatient(request, dentistId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
