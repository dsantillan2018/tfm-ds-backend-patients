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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private PatchValidator patchValidator;

    @Mock
    private PatchMapper patchMapper;

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

    @Test
    void getPatientByIdAndDentistId_returnsMappedPatient() {
        long patientId = 1L;
        UUID dentistId = UUID.randomUUID();
        Patient patient = Patient.builder().patientId(patientId).firstName("Jane").build();
        PatientResponseDto response =
                PatientResponseDto.builder().patientId(patientId).firstName("Jane").build();

        when(patientRepository.findPatientByIdAndDentistId(patientId, dentistId))
                .thenReturn(Optional.of(patient));
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        assertSame(response, patientService.getPatientByIdAndDentistId(patientId, dentistId));

        verify(patientRepository).findPatientByIdAndDentistId(patientId, dentistId);
        verify(patientMapper).toPatientResponseDto(patient);
        verifyNoMoreInteractions(patientRepository, patientMapper);
    }

    @Test
    void getPatientByIdAndDentistId_throwsWhenPatientDoesNotExist() {
        long patientId = 1L;
        UUID dentistId = UUID.randomUUID();

        when(patientRepository.findPatientByIdAndDentistId(patientId, dentistId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.getPatientByIdAndDentistId(patientId, dentistId));

        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository).findPatientByIdAndDentistId(patientId, dentistId);
        verifyNoMoreInteractions(patientRepository, patientMapper);
    }

    @Test
    void updatePatient_appliesValidatedPatchAndSavesPatient() {
        String request = "{\"firstName\":\"Janet\"}";
        long patientId = 1L;
        long version = 3L;
        UUID dentistId = UUID.randomUUID();
        Patient originalPatient = Patient.builder()
                .patientId(patientId)
                .version(version)
                .build();
        PatientDto originalDto = PatientDto.builder().firstName("Jane").build();
        PatientDto patchedDto = PatientDto.builder().firstName("Janet").build();
        Patient updatedPatient = Patient.builder()
                .patientId(patientId)
                .firstName("Janet")
                .version(version + 1)
                .build();
        PatientResponseDto response = PatientResponseDto.builder()
                .patientId(patientId)
                .firstName("Janet")
                .build();

        when(patientRepository.findPatientByIdAndDentistId(patientId, dentistId))
                .thenReturn(Optional.of(originalPatient));
        when(patientMapper.toPatientDto(originalPatient)).thenReturn(originalDto);
        when(patchMapper.applyPatch(request, originalDto)).thenReturn(patchedDto);
        when(patientRepository.update(originalPatient)).thenReturn(updatedPatient);
        when(patientMapper.toPatientResponseDto(updatedPatient)).thenReturn(response);

        assertSame(response, patientService.updatePatient(request, patientId, dentistId, version));

        verify(patchValidator).validateFields(request, Set.of(
                "firstName", "lastName", "email", "documentNumber", "documentType",
                "birthDate", "phoneNumber", "gender"));
        verify(patientRepository).findPatientByIdAndDentistId(patientId, dentistId);
        verify(patientMapper).toPatientDto(originalPatient);
        verify(patchMapper).applyPatch(request, originalDto);
        verify(patchValidator).validatePatch(patchedDto);
        verify(patientMapper).updatePatientFromDto(patchedDto, originalPatient);
        verify(patientRepository).update(originalPatient);
        verify(patientMapper).toPatientResponseDto(updatedPatient);
        verifyNoMoreInteractions(patientRepository, patientMapper, patchValidator, patchMapper);
    }

    @Test
    void updatePatient_throwsWhenPatientDoesNotExist() {
        String request = "{\"firstName\":\"Janet\"}";
        long patientId = 1L;
        UUID dentistId = UUID.randomUUID();

        when(patientRepository.findPatientByIdAndDentistId(patientId, dentistId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.updatePatient(request, patientId, dentistId, 0L));

        verify(patchValidator).validateFields(request, Set.of(
                "firstName", "lastName", "email", "documentNumber", "documentType",
                "birthDate", "phoneNumber", "gender"));
        verify(patientRepository).findPatientByIdAndDentistId(patientId, dentistId);
        verifyNoInteractions(patientMapper, patchMapper);
        verifyNoMoreInteractions(patientRepository, patchValidator);
    }

    @Test
    void updatePatient_throwsWhenVersionDoesNotMatch() {
        String request = "{\"firstName\":\"Janet\"}";
        long patientId = 1L;
        UUID dentistId = UUID.randomUUID();
        Patient originalPatient = Patient.builder().patientId(patientId).version(2L).build();

        when(patientRepository.findPatientByIdAndDentistId(patientId, dentistId))
                .thenReturn(Optional.of(originalPatient));

        assertThrows(
                PreconditionFailedException.class,
                () -> patientService.updatePatient(request, patientId, dentistId, 1L));

        verify(patchValidator).validateFields(request, Set.of(
                "firstName", "lastName", "email", "documentNumber", "documentType",
                "birthDate", "phoneNumber", "gender"));
        verify(patientRepository).findPatientByIdAndDentistId(patientId, dentistId);
        verifyNoInteractions(patientMapper, patchMapper);
        verifyNoMoreInteractions(patientRepository, patchValidator);
    }

    @Test
    void updatePatient_stopsWhenFieldsAreInvalid() {
        String request = "{\"status\":\"INACTIVE\"}";
        long patientId = 1L;
        UUID dentistId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("Field is not allowed for update: status"))
                .when(patchValidator).validateFields(eq(request), anySet());

        assertThrows(
                IllegalArgumentException.class,
                () -> patientService.updatePatient(request, patientId, dentistId, 0L));

        verify(patchValidator).validateFields(request, Set.of(
                "firstName", "lastName", "email", "documentNumber", "documentType",
                "birthDate", "phoneNumber", "gender"));
        verifyNoInteractions(patientRepository, patientMapper, patchMapper);
        verifyNoMoreInteractions(patchValidator);
    }

    @Test
    void updatePatient_doesNotSaveWhenPatchedDtoIsInvalid() {
        String request = "{\"firstName\":\"\"}";
        long patientId = 1L;
        UUID dentistId = UUID.randomUUID();
        Patient originalPatient = Patient.builder().patientId(patientId).version(0L).build();
        PatientDto originalDto = PatientDto.builder().firstName("Jane").build();
        PatientDto patchedDto = PatientDto.builder().firstName("").build();

        when(patientRepository.findPatientByIdAndDentistId(patientId, dentistId))
                .thenReturn(Optional.of(originalPatient));
        when(patientMapper.toPatientDto(originalPatient)).thenReturn(originalDto);
        when(patchMapper.applyPatch(request, originalDto)).thenReturn(patchedDto);
        doThrow(new IllegalArgumentException("Invalid patch object"))
                .when(patchValidator).validatePatch(patchedDto);

        assertThrows(
                IllegalArgumentException.class,
                () -> patientService.updatePatient(request, patientId, dentistId, 0L));

        verify(patchValidator).validateFields(request, Set.of(
                "firstName", "lastName", "email", "documentNumber", "documentType",
                "birthDate", "phoneNumber", "gender"));
        verify(patientRepository).findPatientByIdAndDentistId(patientId, dentistId);
        verify(patientMapper).toPatientDto(originalPatient);
        verify(patchMapper).applyPatch(request, originalDto);
        verify(patchValidator).validatePatch(patchedDto);
        verify(patientRepository, never()).update(originalPatient);
        verifyNoMoreInteractions(patientRepository, patientMapper, patchMapper, patchValidator);
    }

    @Test
    void removePatient_marksAssociationAsInactive() {
        long patientId = 1L;
        UUID dentistId = UUID.randomUUID();
        PatientDentist patientDentist = PatientDentist.builder()
                .patient(Patient.builder().patientId(patientId).build())
                .dentistId(dentistId)
                .status(PatientStatus.ACTIVE)
                .build();

        when(patientRepository.findPatientDentistByPatientIdAndDentistId(patientId, dentistId))
                .thenReturn(Optional.of(patientDentist));

        patientService.removePatient(patientId, dentistId);

        assertEquals(PatientStatus.INACTIVE, patientDentist.getStatus());
        verify(patientRepository).findPatientDentistByPatientIdAndDentistId(patientId, dentistId);
        verifyNoMoreInteractions(patientRepository, patientMapper);
    }

    @Test
    void removePatient_throwsWhenAssociationDoesNotExist() {
        long patientId = 1L;
        UUID dentistId = UUID.randomUUID();

        when(patientRepository.findPatientDentistByPatientIdAndDentistId(patientId, dentistId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.removePatient(patientId, dentistId));

        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository).findPatientDentistByPatientIdAndDentistId(patientId, dentistId);
        verifyNoMoreInteractions(patientRepository, patientMapper);
    }
}
