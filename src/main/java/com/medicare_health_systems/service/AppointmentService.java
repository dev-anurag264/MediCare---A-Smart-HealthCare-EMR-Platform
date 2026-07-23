package com.medicare_health_systems.service;


import com.medicare_health_systems.dto.request.BookingAppointmentRequest;
import com.medicare_health_systems.dto.response.AppointmentResponse;
import com.medicare_health_systems.dto.response.BookingAppointmentResponse;
import com.medicare_health_systems.entity.*;
import com.medicare_health_systems.events.AppointmentBookedEvent;
import com.medicare_health_systems.exceptions.AppointmentClashException;
import com.medicare_health_systems.exceptions.InvalidAppointmentStatusException;
import com.medicare_health_systems.exceptions.ResourceNotFound;
import com.medicare_health_systems.repository.AppointmentRepository;
import com.medicare_health_systems.repository.DoctorProfileRepo;
import com.medicare_health_systems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepo doctorProfileRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;


    //book appointment
    @Transactional
    public AppointmentResponse bookAppointment(BookingAppointmentRequest request){
            User patient = userService.getAuthenticatedUser();

            // fetch and validate doctor
        User doctor = userRepository.findById(request.getDoctorId()).
                filter( u -> u.getRole() == Role.DOCTOR)
                .orElseThrow(()-> new ResourceNotFound("Doctor","id",request.getDoctorId()));

        DoctorProfile profile = doctorProfileRepository.findByUserId(doctor.getId()).
                orElseThrow(() -> new ResourceNotFound("Doctor not found. Profile not set yet"));

        //working day validation
        //MON,TUE,WED,FRI
        String dayCode = request.getAppointmentDate().getDayOfWeek().name().substring(0,3);
        if(!profile.getAvailableDays().contains(dayCode)){
            throw new InvalidAppointmentStatusException(
                    "Dr."+ doctor.getFirstName() + " is not available on "+ request.getAppointmentDate().getDayOfWeek().name().toLowerCase()
            );
        }

        //Calculate timing
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = startTime.plusMinutes(profile.getSlotDurationMinutes());

        //Validate if slot is within available days
        if (startTime.isBefore(profile.getAvailableFrom()) || endTime.isAfter(profile.getAvailableTo())) {
            throw new InvalidAppointmentStatusException(
                    String.format("Slot %s–%s is outside doctor's working hours (%s–%s)",
                            startTime, endTime, profile.getAvailableFrom(), profile.getAvailableTo()));
        }

        validateSlotAlignment(startTime,profile.getAvailableFrom(),profile.getSlotDurationMinutes());

        //IMP --
        /**
         * Check for double booking --> using optimistic locking
         **/
        boolean hasConflict = appointmentRepository.existsOverlappingAppointment(
                doctor.getId(), request.getAppointmentDate(), startTime, endTime);

        if(hasConflict){
            throw new AppointmentClashException(String.format("Dr. %s %s is not available on %s at %s. Please choose another slot.",
                    doctor.getFirstName(), doctor.getLastName(),
                    request.getAppointmentDate(), startTime));
        }
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.PENDING)
                .reason(request.getReason())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment booked. ID: {}, Patient: {}, Doctor: {}, Date: {} {}",
                saved.getId(), patient.getEmail(), doctor.getEmail(),
                request.getAppointmentDate(), startTime);

        eventPublisher.publishEvent(
                new AppointmentBookedEvent(
                        this,
                        saved.getId(),
                        patient.getEmail(),
                        patient.getFirstName(),
                        doctor.getFirstName() + " " + doctor.getLastName(),
                        saved.getAppointmentDate().toString(),
                        saved.getStartTime().toString()
                )
        );

        return mapToResponse(saved);
    }

    //confirm pending booking
    @Transactional
    public AppointmentResponse confirmAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        User currentUser = userService.getAuthenticatedUser();

        validateDoctorOwnership(currentUser, appointment);

        appointment.transitionTo(AppointmentStatus.CONFIRMED);  // Uses state machine in entity
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment {} confirmed by doctor {}", appointmentId, currentUser.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        User currentUser = userService.getAuthenticatedUser();

        if (currentUser.getRole() == Role.PATIENT) {
            if (!appointment.getPatient().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("You can only cancel your own appointments");
            }
        }

        appointment.transitionTo(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment {} cancelled by {}", appointmentId, currentUser.getEmail());
        return mapToResponse(saved);
    }
    @Transactional
    public AppointmentResponse completeAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        User currentUser = userService.getAuthenticatedUser();

        validateDoctorOwnership(currentUser, appointment);

        appointment.transitionTo(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);
        return mapToResponse(saved);
    }


    @Transactional
    public AppointmentResponse markNoShow(Long appointmentId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        User currentUser = userService.getAuthenticatedUser();

        validateDoctorOwnership(currentUser, appointment);

        appointment.transitionTo(AppointmentStatus.NO_SHOW);
        return mapToResponse(appointmentRepository.save(appointment));
    }


    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getMyAppointments(int page, int size) {
        User patient = userService.getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size);
        return appointmentRepository.findByPatientId(patient.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(int page, int size) {
        User doctor = userService.getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size);
        return appointmentRepository.findByDoctorId(doctor.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFound("Appointment", "id", appointmentId));

        User currentUser = userService.getAuthenticatedUser();
        // Patients can only view their own appointments
        if (currentUser.getRole() == Role.PATIENT &&
                !appointment.getPatient().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Access denied");
        }

        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentDate").descending());
        return appointmentRepository.findAll(pageable).map(this::mapToResponse);
    }



    //validate slot
    private void validateSlotAlignment(LocalTime startTime, LocalTime availableFrom, int slotMinutes) {
        long minutesFromStart = java.time.Duration.between(availableFrom, startTime).toMinutes();
        if (minutesFromStart < 0 || minutesFromStart % slotMinutes != 0) {
            throw new InvalidAppointmentStatusException(
                    String.format("Start time %s does not align with the %d-minute slot grid starting at %s",
                            startTime, slotMinutes, availableFrom));
        }
    }



    private Appointment getAppointmentOrThrow(Long id) {
        return appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFound("Appointment", "id", id));
    }

    private void validateDoctorOwnership(User currentUser, Appointment appointment) {
        if (currentUser.getRole() == Role.DOCTOR &&
                !appointment.getDoctor().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You can only manage your own appointments");
        }
    }

    private AppointmentResponse mapToResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName())
                .doctorId(a.getDoctor().getId())
                .doctorName("Dr. " + a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName())
                .doctorSpecialty(getDoctorSpecialty(a.getDoctor().getId()))
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .reason(a.getReason())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private String getDoctorSpecialty(Long doctorId) {
        return doctorProfileRepository.findByUserId(doctorId)
                .map(DoctorProfile::getSpecialty)
                .orElse("General");
    }
}
