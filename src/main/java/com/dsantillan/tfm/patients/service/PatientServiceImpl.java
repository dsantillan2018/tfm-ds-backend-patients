package com.dsantillan.tfm.patients.service;

import com.dsantillan.tfm.common.exception.ResourceNotFoundException;
import com.dsantillan.tfm.common.model.dto.PageResponseDto;
import com.dsantillan.tfm.patients.mapper.PatientMapper;
import com.dsantillan.tfm.patients.model.dto.PatientDto;
import com.dsantillan.tfm.patients.model.dto.PatientResponseDto;
import com.dsantillan.tfm.patients.model.entity.Patient;
import com.dsantillan.tfm.patients.model.entity.PatientDentist;
import com.dsantillan.tfm.patients.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Override
    public PatientResponseDto getPatientByIdAndDentistId(long patientId, UUID dentistId) {
        return patientRepository.findPatientByIdAndDentistId(patientId, dentistId)
                .map(patientMapper::toPatientResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    @Override
    public PageResponseDto<PatientResponseDto> getPatientsByDentistId(UUID dentistId, Pageable pageable) {
        Page<PatientResponseDto> patients = patientRepository
                .findPatientsByDentistId(dentistId, pageable)
                .map(patientMapper::toPatientResponseDto);
        return PageResponseDto.<PatientResponseDto>builder()
                .totalCount(patients.getTotalElements())
                .page(patients.getNumber())
                .size(patients.getSize())
                .content(patients.getContent())
                .build();
    }

    @Override
    @Transactional
    public PatientResponseDto createPatient(PatientDto request, UUID dentistId) {
        Patient patient = patientMapper.toPatient(request);
        Patient savedPatient = patientRepository.save(patient);
        PatientDentist patientDentist = PatientDentist.builder()
                .patient(savedPatient)
                .dentistId(dentistId)
                .build();
        patientRepository.savePatientDentist(patientDentist);
        return patientMapper.toPatientResponseDto(savedPatient);
    }
}
