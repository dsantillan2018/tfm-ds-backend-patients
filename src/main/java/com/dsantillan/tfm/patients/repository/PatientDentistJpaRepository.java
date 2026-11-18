package com.dsantillan.tfm.patients.repository;

import com.dsantillan.tfm.patients.model.entity.PatientDentist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientDentistJpaRepository extends JpaRepository<PatientDentist, Long> {
}
