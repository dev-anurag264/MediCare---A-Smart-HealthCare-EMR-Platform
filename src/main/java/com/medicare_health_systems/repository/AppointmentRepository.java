package com.medicare_health_systems.repository;

import com.medicare_health_systems.entity.Appointment;
import com.medicare_health_systems.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,Long> {

    //DOUBLE-BOOKING PREVENTION
//
//     1. Checks if a doctor already has an ACTIVE (non-cancelled) appointment
//      that overlaps with the requested time slot.
//
//     2. Overlap logic: two slots overlap if:
//       requested.startTime < existing.endTime
//       AND requested.endTime > existing.startTime
//
//      Example: existing slot 10:00–10:30
//     *   Request 09:30–10:00 → endTime(10:00) > startTime(10:00)? NO → no overlap ✓
//     *   Request 10:00–10:30 → endTime(10:30) > startTime(10:00)? YES → OVERLAP ✗
//     *   Request 10:15–10:45 → startTime(10:15) < endTime(10:30)? YES → OVERLAP ✗
//     *   Request 10:30–11:00 → startTime(10:30) < endTime(10:30)? NO → no overlap ✓

    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.appointmentDate = :date
          AND a.status NOT IN ('CANCELLED')
          AND a.startTime < :endTime
          AND a.endTime > :startTime
        """)
    boolean existsOverlappingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    //rescheduling
    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.appointmentDate = :date
          AND a.id != :excludeId
          AND a.status NOT IN ('CANCELLED')
          AND a.startTime < :endTime
          AND a.endTime > :startTime
        """)
    boolean existsOverlappingAppointmentExcluding(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query(value = """
        SELECT a FROM Appointment a
        JOIN FETCH a.doctor d
        JOIN FETCH a.patient p
        WHERE a.patient.id = :patientId
        ORDER BY a.appointmentDate DESC, a.startTime DESC
        """,
            countQuery = "SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId")
    Page<Appointment> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // DOCTOR QUERIES
    // ─────────────────────────────────────────────────────────────

    /**
     * All appointments for a doctor on a specific date.
     * Used to: show today's schedule, generate available slots.
     */
    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.patient p
        WHERE a.doctor.id = :doctorId
          AND a.appointmentDate = :date
          AND a.status NOT IN ('CANCELLED')
        ORDER BY a.startTime ASC
        """)
    List<Appointment> findDoctorAppointmentsOnDate(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date
    );

    /**
     * Paginated appointments for a doctor — for the doctor's dashboard.
     */
    @Query(value = """
        SELECT a FROM Appointment a
        JOIN FETCH a.patient p
        WHERE a.doctor.id = :doctorId
        ORDER BY a.appointmentDate DESC, a.startTime DESC
        """,
            countQuery = "SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId")
    Page<Appointment> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    /**
     * Doctor's upcoming appointments — status CONFIRMED, date >= today.
     */
    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.patient p
        WHERE a.doctor.id = :doctorId
          AND a.status = 'CONFIRMED'
          AND a.appointmentDate >= :fromDate
        ORDER BY a.appointmentDate ASC, a.startTime ASC
        """)
    List<Appointment> findUpcomingForDoctor(
            @Param("doctorId") Long doctorId,
            @Param("fromDate") LocalDate fromDate
    );


    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.patient p
        JOIN FETCH a.doctor d
        WHERE a.id = :id
        """)
    Optional<Appointment> findByIdWithDetails(@Param("id") Long id);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.id = :id")
    Optional<Appointment> findByIdWithLock(@Param("id") Long id);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

}
