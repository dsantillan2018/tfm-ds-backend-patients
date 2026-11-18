package com.dsantillan.tfm.patients.service;

import com.dsantillan.tfm.patients.model.dto.PatientDto;
import com.dsantillan.tfm.patients.model.dto.PatientResponseDto;

import java.util.UUID;

public interface PatientService {

    PatientResponseDto createPatient(PatientDto request, UUID dentistId);
}
