package com.dsantillan.tfm.patients.model.entity;

import com.dsantillan.tfm.common.audit.entity.AuditableEntity;
import com.dsantillan.tfm.patients.model.enums.PatientStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tfm_patients_dentists")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientDentist extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_dentist_id")
    private Long patientDentistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "dentist_id", nullable = false)
    private UUID dentistId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PatientStatus status = PatientStatus.ACTIVE;
}
