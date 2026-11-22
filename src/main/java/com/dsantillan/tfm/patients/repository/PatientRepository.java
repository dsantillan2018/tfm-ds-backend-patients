package com.dsantillan.tfm.patients.repository;

import com.dsantillan.tfm.patients.model.entity.Patient;
import com.dsantillan.tfm.patients.model.entity.PatientDentist;
import com.dsantillan.tfm.patients.model.enums.PatientStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PatientRepository {

    private final PatientJpaRepository patientJpaRepository;
    private final PatientDentistJpaRepository patientDentistJpaRepository;

    public Page<Patient> findPatientsByDentistId(UUID dentistId, Pageable pageable) {
        return patientDentistJpaRepository.findPatientsByDentistId(dentistId, PatientStatus.ACTIVE, pageable);
    }

    public Patient save(Patient patient) {
        return patientJpaRepository.save(patient);
    }

    public void savePatientDentist(PatientDentist patientDentist) {
        patientDentistJpaRepository.save(patientDentist);
    }
}
