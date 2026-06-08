package com.medicare_health_systems.service;

import com.medicare_health_systems.dto.request.CreateMedicalRequest;
import com.medicare_health_systems.dto.request.MedicalRecordResponse;
import com.medicare_health_systems.entity.*;
import com.medicare_health_systems.exceptions.AppointmentClashException;
import com.medicare_health_systems.exceptions.ResourceNotFound;
import com.medicare_health_systems.repository.AppointmentRepository;
import com.medicare_health_systems.repository.MedicalDocumentRepository;
import com.medicare_health_systems.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository  medicalRecordRepository;
    private final MedicalDocumentRepository medicalDocumentRepository;
    private final AppointmentRepository appointmentRepository;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @Transactional
    public MedicalRecordResponse createMedicalRecord(CreateMedicalRequest request){
        User doctor = userService.getAuthenticatedUser();

        //fetch appointment
        Appointment appointment = appointmentRepository.findByIdWithDetails(request.getAppointmentId()).orElseThrow(
                () -> new ResourceNotFound("Appointment does not exists"));

        log.info("Logged in doctor ID: {}", doctor.getId());
        log.info("Appointment doctor ID: {}", appointment.getDoctor().getId());
        //check for completed appointments
        if(appointment.getStatus() != AppointmentStatus.COMPLETED){
            throw new IllegalStateException("EMR can only be created for Completed appointments");
        }

        //doctor must own the appointment
        if(!appointment.getDoctor().getId().equals(doctor.getId())){
            throw new IllegalStateException("You can only create EMR for your own appointment");
        }
        if (medicalRecordRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new AppointmentClashException(
                    "Medical record already exists for appointment ID: " + request.getAppointmentId());
        }

        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .doctor(doctor)
                .visitDate(appointment.getAppointmentDate())
                .complaint(request.getComplaint())
                .build();

        request.getDiagnoses().forEach(dr -> {
            Diagnosis diagnosis = Diagnosis.builder()
                    .icdCode(dr.getIcdCode())
                    .description(dr.getDescription())
                    .notes(dr.getNotes())
                    .build();
            record.addDiagnosis(diagnosis); // sets back-reference automatically
        });

        if (request.getPrescriptions() != null) {
            request.getPrescriptions().forEach(pr -> {
                Prescription prescription = Prescription.builder()
                        .medicineName(pr.getMedicineName())
                        .dosage(pr.getDosage())
                        .frequency(pr.getFrequency())
                        .durationDays(pr.getDurationDays())
                        .instructions(pr.getInstructions())
                        .build();
                record.addPrescription(prescription);
            });
        }

        // One save() — CascadeType.ALL handles diagnoses and prescriptions
        MedicalRecord saved = medicalRecordRepository.save(record);
        log.info("Medical record created. ID: {}, AppointmentID: {}, Doctor: {}",
                saved.getId(), request.getAppointmentId(), doctor.getEmail());

        return mapToResponse(saved);

    }


    @Transactional
    public MedicalRecordResponse.MedicalDocumentResponse uploadDocument(
            MultipartFile file, Long patientId, Long medicalRecordId, DocumentType documentType) {

        User uploader = userService.getAuthenticatedUser();

        // Resolve patient — doctor uploads on behalf of patient, or patient uploads self
        User patient;
        if (uploader.getRole() == Role.PATIENT) {
            patient = uploader; // patient uploads their own document
        } else {
            // Doctor or admin uploading for a specific patient
            patient = userService.getUserEntityById(patientId);
        }

        // Step 1: Save file to disk
        String storedFilename = fileStorageService.storeFile(file);

        // Step 2: Build and save metadata
        MedicalDocument.MedicalDocumentBuilder builder = MedicalDocument.builder()
                .patient(patient)
                .uploadedBy(uploader)
                .originalName(file.getOriginalFilename())
                .storedName(storedFilename)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(fileStorageService.getUploadDir() + "/" + storedFilename)
                .documentType(documentType != null ? documentType : DocumentType.OTHER);

        // Link to medical record if provided
        if (medicalRecordId != null) {
            MedicalRecord record = medicalRecordRepository.findById(medicalRecordId)
                    .orElseThrow(() -> new ResourceNotFound(
                            "MedicalRecord", "id", medicalRecordId));
            builder.medicalRecord(record);
        }

        MedicalDocument saved = medicalDocumentRepository.save(builder.build());
        log.info("Document uploaded: {} (stored as: {})", file.getOriginalFilename(), storedFilename);

        return mapDocumentToResponse(saved);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordById(Long id) {
        MedicalRecord record = medicalRecordRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFound("MedicalRecord", "id", id));

        User currentUser = userService.getAuthenticatedUser();

        // Patients can only view their own records
        if (currentUser.getRole() == Role.PATIENT &&
                !record.getPatient().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Access denied to this medical record");
        }

        return mapToResponse(record);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getRecordByAppointmentId(Long appointmentId) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFound(
                        "MedicalRecord for appointment", "appointmentId", appointmentId));

        User currentUser = userService.getAuthenticatedUser();
        if (currentUser.getRole() == Role.PATIENT &&
                !record.getPatient().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Access denied");
        }

        return mapToResponse(record);
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> getPatientRecords(Long patientId, int page, int size) {
        User currentUser = userService.getAuthenticatedUser();

        // Patient can only view own history
        if (currentUser.getRole() == Role.PATIENT &&
                !currentUser.getId().equals(patientId)) {
            throw new IllegalStateException("Access denied");
        }

        Pageable pageable = PageRequest.of(page, size);
        return medicalRecordRepository.findByPatientId(patientId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> getMyRecordsAsDoctor(int page, int size) {
        User doctor = userService.getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size);
        return medicalRecordRepository.findByDoctorId(doctor.getId(), pageable)
                .map(this::mapToResponse);
    }


    //helper functions
    private MedicalRecordResponse mapToResponse(MedicalRecord r) {
        return MedicalRecordResponse.builder()
                .id(r.getId())
                .appointmentId(r.getAppointment().getId())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getFirstName() + " " + r.getPatient().getLastName())
                .doctorId(r.getDoctor().getId())
                .doctorName("Dr. " + r.getDoctor().getFirstName() + " " + r.getDoctor().getLastName())
                .visitDate(r.getVisitDate())
                .chiefComplaint(r.getComplaint())
                .diagnoses(r.getDiagnoses().stream().map(this::mapDiagnosis).collect(Collectors.toList()))
                .prescriptions(r.getPrescriptions().stream().map(this::mapPrescription).collect(Collectors.toList()))
                .documents(r.getDocuments().stream().map(this::mapDocumentToResponse).collect(Collectors.toList()))
                .createdAt(r.getCreatedAt())
                .build();
    }
    private MedicalRecordResponse.DiagnosisResponse mapDiagnosis(Diagnosis d) {
        return MedicalRecordResponse.DiagnosisResponse.builder()
                .id(d.getId())
                .icdCode(d.getIcdCode())
                .description(d.getDescription())
                .notes(d.getNotes())
                .build();
    }

    private MedicalRecordResponse.PrescriptionResponse mapPrescription(Prescription p) {
        return MedicalRecordResponse.PrescriptionResponse.builder()
                .id(p.getId())
                .medicineName(p.getMedicineName())
                .dosage(p.getDosage())
                .frequency(p.getFrequency())
                .durationDays(p.getDurationDays())
                .instructions(p.getInstructions())
                .build();
    }
    private MedicalRecordResponse.MedicalDocumentResponse mapDocumentToResponse(MedicalDocument d) {
        return MedicalRecordResponse.MedicalDocumentResponse.builder()
                .id(d.getId())
                .originalName(d.getOriginalName())
                .fileType(d.getFileType())
                .fileSize(d.getFileSize())
                .documentType(d.getDocumentType())
                .downloadUrl("/api/v1/documents/" + d.getId() + "/download")
                .createdAt(d.getCreatedAt())
                .build();
    }

}
