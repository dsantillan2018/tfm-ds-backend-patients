package com.dsantillan.tfm.patients.repository;

import com.dsantillan.tfm.patients.model.entity.Patient;
import com.dsantillan.tfm.patients.model.entity.PatientDentist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PatientRepository {

    private final PatientJpaRepository patientJpaRepository;
    private final PatientDentistJpaRepository patientDentistJpaRepository;

    public Patient save(Patient patient) {
        return patientJpaRepository.save(patient);
    }

    public void savePatientDentist(PatientDentist patientDentist) {
        patientDentistJpaRepository.save(patientDentist);
    }
}
