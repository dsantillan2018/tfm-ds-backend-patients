package com.dsantillan.tfm.patients.service;

import com.dsantillan.tfm.common.exception.PreconditionFailedException;
import com.dsantillan.tfm.common.exception.ResourceNotFoundException;
import com.dsantillan.tfm.common.mapper.PatchMapper;
import com.dsantillan.tfm.common.model.dto.PageResponseDto;
import com.dsantillan.tfm.common.validation.PatchValidator;
import com.dsantillan.tfm.patients.mapper.PatientMapper;
import com.dsantillan.tfm.patients.model.dto.PatientDto;
import com.dsantillan.tfm.patients.model.dto.PatientResponseDto;
import com.dsantillan.tfm.patients.model.entity.Patient;
import com.dsantillan.tfm.patients.model.entity.PatientDentist;
import com.dsantillan.tfm.patients.model.enums.PatientStatus;
import com.dsantillan.tfm.patients.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    private final PatientMapper patientMapper;

    private final PatchValidator patchValidator;

    private final PatchMapper patchMapper;

    // Define the allowed fields for patching
    private static final Set<String> ALLOWED_PATCH_FIELDS = Set.of(
            "firstName",
            "lastName",
            "email",
            "documentNumber",
            "documentType",
            "birthDate",
            "phoneNumber",
            "gender"
    );

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

    @Override
    @Transactional
    public PatientResponseDto updatePatient(String request, long patientId, UUID dentistId, long version) {
        // Validate the fields in the patch request against the allowed fields for patching
        patchValidator.validateFields(request, ALLOWED_PATCH_FIELDS);

        // Fetch the original patient entity to be updated
        Patient originalPatient = patientRepository.findPatientByIdAndDentistId(patientId, dentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Validate the expected version
        if (originalPatient.getVersion() != version) {
            throw new PreconditionFailedException(
                    "Patient has been modified by another user, check new changes and try again."
            );
        }

        // Apply the patch to the original patient entity and map it to a PatientDto
        PatientDto patchedPatientDto = patchMapper.applyPatch(request, patientMapper.toPatientDto(originalPatient));

        // Validate the patched PatientDto
        patchValidator.validatePatch(patchedPatientDto);

        // Update the original patient entity with the patched PatientDto
        patientMapper.updatePatientFromDto(patchedPatientDto, originalPatient);

        // Save the updated patient entity to the database
        Patient updatedPatient = patientRepository.update(originalPatient);

        return patientMapper.toPatientResponseDto(updatedPatient);
    }

    @Override
    @Transactional
    public void removePatient(long patientId, UUID dentistId) {
        PatientDentist patientDentist = patientRepository
                .findPatientDentistByPatientIdAndDentistId(patientId, dentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        // Mark the patientDentist as inactive instead of deleting it through dirty checking the status
        patientDentist.setStatus(PatientStatus.INACTIVE);
    }
}
