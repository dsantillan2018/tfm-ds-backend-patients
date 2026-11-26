package com.dsantillan.tfm.patients.model.entity;

import com.dsantillan.tfm.common.audit.entity.AuditableEntity;
import com.dsantillan.tfm.common.identification.model.enums.IdentificationType;
import com.dsantillan.tfm.common.model.enums.Gender;
import com.dsantillan.tfm.patients.model.enums.PatientStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tfm_patients")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Patient extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "document_number", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private IdentificationType documentType;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PatientStatus status = PatientStatus.ACTIVE;

    // Version field for optimistic locking, not included in dto and not exposed in API responses
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
