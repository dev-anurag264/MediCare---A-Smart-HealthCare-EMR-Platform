package com.medicare_health_systems.repository;

import com.medicare_health_systems.entity.MedicalDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {
    @Query("""
        SELECT d FROM MedicalDocument d
        JOIN FETCH d.patient
        JOIN FETCH d.uploadedBy
        WHERE d.id = :id
        """)
    Optional<MedicalDocument> findByIdWithDetails(@Param("id") Long id);

    @Query(value = """
        SELECT d FROM MedicalDocument d
        WHERE d.patient.id = :patientId
        ORDER BY d.createdAt DESC
        """,
            countQuery = "SELECT COUNT(d) FROM MedicalDocument d WHERE d.patient.id = :patientId")
    Page<MedicalDocument> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);

    @Query("""
        SELECT d FROM MedicalDocument d
        WHERE d.medicalRecord.id = :recordId
        ORDER BY d.createdAt DESC
        """)
    List<MedicalDocument> findByMedicalRecordId(@Param("recordId") Long recordId);
}
