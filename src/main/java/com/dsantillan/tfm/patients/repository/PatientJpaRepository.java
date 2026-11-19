package com.dsantillan.tfm.patients.repository;

import com.dsantillan.tfm.patients.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientJpaRepository extends JpaRepository<Patient, Long> {
}
