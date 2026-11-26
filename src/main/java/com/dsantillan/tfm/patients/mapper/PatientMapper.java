package com.dsantillan.tfm.patients.mapper;

import com.dsantillan.tfm.patients.model.dto.PatientDto;
import com.dsantillan.tfm.patients.model.dto.PatientResponseDto;
import com.dsantillan.tfm.patients.model.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "status", ignore = true)
    Patient toPatient(PatientDto patientDto);

    PatientResponseDto toPatientResponseDto(Patient patient);

    PatientDto toPatientDto(Patient patient);

    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updatePatientFromDto(PatientDto patientDto, @MappingTarget Patient patient);
}
