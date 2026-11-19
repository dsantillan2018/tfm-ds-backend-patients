package com.dsantillan.tfm.patients.service;

import com.dsantillan.tfm.common.model.dto.PageResponseDto;
import com.dsantillan.tfm.patients.mapper.PatientMapper;
import com.dsantillan.tfm.patients.model.dto.PatientDto;
import com.dsantillan.tfm.patients.model.dto.PatientResponseDto;
import com.dsantillan.tfm.patients.model.entity.Patient;
import com.dsantillan.tfm.patients.model.entity.PatientDentist;
import com.dsantillan.tfm.patients.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    void getPatientsByDentistId_returnsMappedPageResponse() {
        UUID dentistId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(1, 2);
        Patient patient = Patient.builder().patientId(1L).build();
        PatientResponseDto response = PatientResponseDto.builder().patientId(1L).build();
        Page<Patient> page = new PageImpl<>(List.of(patient), pageable, 5);

        when(patientRepository.findPatientsByDentistId(dentistId, pageable)).thenReturn(page);
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        PageResponseDto<PatientResponseDto> result =
                patientService.getPatientsByDentistId(dentistId, pageable);

        assertEquals(5, result.getTotalCount());
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(List.of(response), result.getContent());
        verify(patientRepository).findPatientsByDentistId(dentistId, pageable);
        verify(patientMapper).toPatientResponseDto(patient);
        verifyNoMoreInteractions(patientRepository, patientMapper);
    }

    @Test
    void createPatient_savesPatientAndPatientDentistAndReturnsResponse() {
        UUID dentistId = UUID.randomUUID();
        PatientDto request = PatientDto.builder().firstName("Jane").build();
        Patient patient = Patient.builder().firstName("Jane").build();
        Patient savedPatient = Patient.builder().patientId(1L).firstName("Jane").build();
        PatientResponseDto response = PatientResponseDto.builder().patientId(1L).build();

        when(patientMapper.toPatient(request)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(savedPatient);
        when(patientMapper.toPatientResponseDto(savedPatient)).thenReturn(response);

        assertSame(response, patientService.createPatient(request, dentistId));

        ArgumentCaptor<PatientDentist> patientDentistCaptor =
                ArgumentCaptor.forClass(PatientDentist.class);
        verify(patientRepository).save(patient);
        verify(patientRepository).savePatientDentist(patientDentistCaptor.capture());
        verify(patientMapper).toPatient(request);
        verify(patientMapper).toPatientResponseDto(savedPatient);
        assertSame(savedPatient, patientDentistCaptor.getValue().getPatient());
        assertEquals(dentistId, patientDentistCaptor.getValue().getDentistId());
        verifyNoMoreInteractions(patientRepository, patientMapper);
    }
}
