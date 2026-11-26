package com.dsantillan.tfm.patients.controller;

import com.dsantillan.tfm.common.model.dto.PageResponseDto;
import com.dsantillan.tfm.patients.model.dto.PatientDto;
import com.dsantillan.tfm.patients.model.dto.PatientResponseDto;
import com.dsantillan.tfm.patients.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PatientsController {

    private final PatientService patientService;

    @GetMapping("/dentists/{dentistId}/patients/{patientId}")
    @PreAuthorize("hasRole(T(com.dsantillan.tfm.common.security.auth.Roles).DENTIST_ROLE) " +
            "and #dentistId == authentication.principal.subject")
    public ResponseEntity<PatientResponseDto> getPatientByIdAndDentistId(
            @PathVariable("patientId") long patientId,
            @PathVariable("dentistId") String dentistId) {
        PatientResponseDto patient = patientService.getPatientByIdAndDentistId(patientId, UUID.fromString(dentistId));
        return ResponseEntity.ok()
                .eTag(String.valueOf(patient.getVersion())) // Set the ETag header with the patient's version
                .body(patient);
    }

    @GetMapping("/dentists/{dentistId}/patients")
    @PreAuthorize("hasRole(T(com.dsantillan.tfm.common.security.auth.Roles).DENTIST_ROLE) " +
            "and #dentistId == authentication.principal.subject")
    public ResponseEntity<PageResponseDto<PatientResponseDto>> getPatientsByDentistId(
            @PathVariable("dentistId") String dentistId,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        PageResponseDto<PatientResponseDto> patients = patientService.
                getPatientsByDentistId(java.util.UUID.fromString(dentistId), pageable);
        return ResponseEntity.ok(patients);
    }

    @PostMapping("/patients")
    @PreAuthorize("hasAnyRole(T(com.dsantillan.tfm.common.security.auth.Roles).DENTIST_ROLE, " +
            "T(com.dsantillan.tfm.common.security.auth.Roles).ADMIN_ROLE)")
    public ResponseEntity<PatientResponseDto> addPatient(
            @Valid @RequestBody PatientDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID dentistId = UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
        PatientResponseDto response = patientService.createPatient(request, dentistId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/dentists/{dentistId}/patients/{patientId}")
    @PreAuthorize("hasRole(T(com.dsantillan.tfm.common.security.auth.Roles).DENTIST_ROLE) " +
            "and #dentistId == authentication.principal.subject")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @PathVariable("patientId") long patientId,
            @PathVariable("dentistId") String dentistId,
            @RequestHeader("If-Match") long version,
            @RequestBody String request) {
        PatientResponseDto response = patientService
                .updatePatient(request, patientId, UUID.fromString(dentistId), version);
        return ResponseEntity.ok()
                .eTag(String.valueOf(response.getVersion())) // Set the ETag header with the updated patient's version
                .body(response);
    }

    @DeleteMapping("/dentists/{dentistId}/patients/{patientId}")
    @PreAuthorize("hasRole(T(com.dsantillan.tfm.common.security.auth.Roles).DENTIST_ROLE) " +
            "and #dentistId == authentication.principal.subject")
    public ResponseEntity<Void> removePatient(
            @PathVariable("patientId") long patientId,
            @PathVariable("dentistId") String dentistId) {
        patientService.removePatient(patientId, UUID.fromString(dentistId));
        return ResponseEntity.noContent().build();
    }
}
