package com.dsantillan.tfm.patients.repository;

import com.dsantillan.tfm.patients.model.entity.Patient;
import com.dsantillan.tfm.patients.model.entity.PatientDentist;
import com.dsantillan.tfm.patients.model.enums.PatientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PatientDentistJpaRepository extends JpaRepository<PatientDentist, Long> {

    @Query("SELECT pd.patient " +
            "FROM PatientDentist pd " +
            "WHERE pd.dentistId = :dentistId " +
            "AND pd.status = :status"
    )
    Page<Patient> findPatientsByDentistId(UUID dentistId, PatientStatus status, Pageable pageable);

    @Query("SELECT pd.patient " +
            "FROM PatientDentist pd " +
            "WHERE pd.dentistId = :dentistId " +
            "AND pd.patient.patientId = :patientId" +
            " AND pd.status = :status"
    )
    Optional<Patient> findPatientByPatientIdAndDentistId(long patientId, UUID dentistId, PatientStatus status);
}
