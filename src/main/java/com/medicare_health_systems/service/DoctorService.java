package com.medicare_health_systems.service;

import com.medicare_health_systems.config.CacheNames;
import com.medicare_health_systems.dto.request.DoctorProfileRequest;
import com.medicare_health_systems.dto.request.UpdatedDoctorProfileRequest;
import com.medicare_health_systems.dto.response.AvailableSlotResponse;
import com.medicare_health_systems.dto.response.DoctorProfileResponse;
import com.medicare_health_systems.entity.DoctorProfile;
import com.medicare_health_systems.entity.Role;
import com.medicare_health_systems.entity.User;
import com.medicare_health_systems.exceptions.ResourceNotFound;
import com.medicare_health_systems.exceptions.UserAlreadyExistsException;
import com.medicare_health_systems.repository.AppointmentRepository;
import com.medicare_health_systems.repository.DoctorProfileRepository;
import com.medicare_health_systems.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    @CacheEvict(value = CacheNames.ALL_DOCTORS, allEntries = true)
    public DoctorProfileResponse createProfile(DoctorProfileRequest request) {
        User principal = userService.getAuthenticatedUser();

        User currentUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFound("User not found"));

        if (currentUser.getRole() != Role.DOCTOR) {
            throw new IllegalStateException("Only DOCTOR role users can create a doctor profile");
        }

        // Guard: profile must not already exist
        if (doctorProfileRepository.existsByUserId(currentUser.getId())) {
            throw new UserAlreadyExistsException(
                    "Doctor profile already exists. Use PUT /api/v1/doctors/profile to update it."
            );
        }

        DoctorProfile profile = DoctorProfile.builder()
                .user(currentUser)
                .specialty(request.getSpecialty())
                .qualification(request.getQualification())
                .experienceYears(request.getExperienceYears())
                .consultationFee(request.getConsultationFee())
                .about(request.getAbout())
                .availableFrom(request.getAvailableFrom() != null
                        ? request.getAvailableFrom() : LocalTime.of(9, 0))
                .availableTo(request.getAvailableTo() != null
                        ? request.getAvailableTo() : LocalTime.of(17, 0))
                .slotDurationMinutes(request.getSlotDurationMinutes() != null
                        ? request.getSlotDurationMinutes() : 30)
                .availableDays(request.getAvailableDays() != null
                        ? request.getAvailableDays() : "MON,TUE,WED,THU,FRI")
                .build();

        profile.setId(currentUser.getId());

        // CRITICAL: tell Spring Data this is a new entity
        // save() checks isNew() > true > em.persist() > INSERT
        // Without this, save() would call em.merge() > StaleObjectStateException
        profile.markAsNew();
        System.out.println("User ID = " + currentUser.getId());

        System.out.println(
                "Managed? " +
                        entityManager.contains(currentUser)
        );
        DoctorProfile saved = doctorProfileRepository.save(profile);
        log.info("Doctor profile CREATED for userId: {}", currentUser.getId());
        return mapToResponse(saved);
    }
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.DOCTOR_PROFILES, key = "#result.userId"),
            @CacheEvict(value = CacheNames.ALL_DOCTORS, allEntries = true),
            @CacheEvict(value = CacheNames.AVAILABLE_SLOTS, allEntries = true)
    })
    public DoctorProfileResponse updateProfile(UpdatedDoctorProfileRequest request) {
        User principal = userService.getAuthenticatedUser();

        User currentUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFound("User not found"));

        // Guard: profile must exist before updating
        DoctorProfile profile = doctorProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFound(
                        "No doctor profile found. Create one first with POST /api/v1/doctors/profile"));


        if (request.getSpecialty() != null)         profile.setSpecialty(request.getSpecialty());
        if (request.getQualification() != null)     profile.setQualification(request.getQualification());
        if (request.getExperienceYears() != null)   profile.setExperienceYears(request.getExperienceYears());
        if (request.getConsultationFee() != null)   profile.setConsultationFee(request.getConsultationFee());
        if (request.getAbout() != null)             profile.setAbout(request.getAbout());
        if (request.getAvailableFrom() != null)     profile.setAvailableFrom(request.getAvailableFrom());
        if (request.getAvailableTo() != null)       profile.setAvailableTo(request.getAvailableTo());
        if (request.getSlotDurationMinutes() != null) profile.setSlotDurationMinutes(request.getSlotDurationMinutes());
        if (request.getAvailableDays() != null)     profile.setAvailableDays(request.getAvailableDays());

        DoctorProfile saved = doctorProfileRepository.save(profile);
        log.info("Doctor profile UPDATED for userId: {}", currentUser.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.ALL_DOCTORS, key = "'all'")
    public List<DoctorProfileResponse> getAllDoctors() {

        return doctorProfileRepository.findAllActiveDoctors()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    //get doctor  profile
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.DOCTOR_PROFILES, key = "#userId", unless = "#result == null")
    public DoctorProfileResponse getDoctorProfile(Long userId) {
        log.debug("Cache MISS — fetching doctor profile from DB for userId: {}", userId);
        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFound("DoctorProfile", "userId", userId));
        return mapToResponse(profile);
    }

    //get available slots
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.AVAILABLE_SLOTS, key = "#doctorId + '_' + #date")
    public List<AvailableSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        log.debug("Cache MISS — generating slots from DB for doctor: {}, date: {}", doctorId, date);
        DoctorProfile profile = doctorProfileRepository.findByUserId(doctorId)
                .orElseThrow(() -> new ResourceNotFound("DoctorProfile", "userId", doctorId));

        String dayCode = date.getDayOfWeek().name().substring(0, 3);
        if (!profile.getAvailableDays().contains(dayCode)) {
            return List.of();
        }

        Set<LocalTime> bookedStartTimes = appointmentRepository
                .findDoctorAppointmentsOnDate(doctorId, date)
                .stream()
                .map(a -> a.getStartTime())
                .collect(Collectors.toSet());

        List<AvailableSlotResponse> slots = new ArrayList<>();
        LocalTime current = profile.getAvailableFrom();
        int durationMinutes = profile.getSlotDurationMinutes();

        while (current.plusMinutes(durationMinutes).compareTo(profile.getAvailableTo()) <= 0) {
            LocalTime end = current.plusMinutes(durationMinutes);
            boolean isAvailable = !bookedStartTimes.contains(current);

            if (date.equals(LocalDate.now()) && current.isBefore(LocalTime.now())) {
                isAvailable = false;
            }

            slots.add(AvailableSlotResponse.builder()
                    .startTime(current)
                    .endTime(end)
                    .available(isAvailable)
                    .build());

            current = end;
        }

        return slots;
    }

    private DoctorProfileResponse mapToResponse(DoctorProfile profile) {
        User user = profile.getUser();
        return DoctorProfileResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .specialty(profile.getSpecialty())
                .qualification(profile.getQualification())
                .experienceYears(profile.getExperienceYears())
                .consultationFee(profile.getConsultationFee())
                .about(profile.getAbout())
                .availableFrom(profile.getAvailableFrom())
                .availableTo(profile.getAvailableTo())
                .slotDurationMinutes(profile.getSlotDurationMinutes())
                .availableDays(profile.getAvailableDays())
                .build();
    }
}
