package com.dsantillan.tfm.patients.service;

import com.dsantillan.tfm.common.model.dto.PageResponseDto;
import com.dsantillan.tfm.patients.model.dto.PatientDto;
import com.dsantillan.tfm.patients.model.dto.PatientResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PatientService {

    PatientResponseDto getPatientByIdAndDentistId(long patientId, UUID dentistId);

    PageResponseDto<PatientResponseDto> getPatientsByDentistId(UUID dentistId, Pageable pageable);

    PatientResponseDto createPatient(PatientDto request, UUID dentistId);

    PatientResponseDto updatePatient(String request, long patientId, UUID dentistId, long version);

    void removePatient(long patientId, UUID dentistId);
}
