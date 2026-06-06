package com.medicare_health_systems.repository;

import com.medicare_health_systems.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    @Query("""
        SELECT DISTINCT mr FROM MedicalRecord mr
        LEFT JOIN FETCH mr.diagnoses
        LEFT JOIN FETCH mr.prescriptions
        LEFT JOIN FETCH mr.documents
        JOIN FETCH mr.patient p
        JOIN FETCH mr.doctor d
        WHERE mr.id = :id
        """)
    Optional<MedicalRecord> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT mr FROM MedicalRecord mr
        JOIN FETCH mr.patient
        JOIN FETCH mr.doctor
        WHERE mr.appointment.id = :appointmentId
        """)
    Optional<MedicalRecord> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);


    @Query(value = """
        SELECT mr FROM MedicalRecord mr
        JOIN FETCH mr.doctor d
        WHERE mr.patient.id = :patientId
        ORDER BY mr.visitDate DESC
        """,
            countQuery = "SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.patient.id = :patientId")
    Page<MedicalRecord> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);


    @Query(value = """
        SELECT mr FROM MedicalRecord mr
        JOIN FETCH mr.patient p
        WHERE mr.doctor.id = :doctorId
        ORDER BY mr.visitDate DESC
        """,
            countQuery = "SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.doctor.id = :doctorId")
    Page<MedicalRecord> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);
}
