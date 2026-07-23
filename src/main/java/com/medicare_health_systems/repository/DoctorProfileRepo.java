package com.medicare_health_systems.repository;

import com.medicare_health_systems.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepo extends JpaRepository<DoctorProfile,Long> {
    Optional<DoctorProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    @Query("SELECT dp FROM DoctorProfile dp JOIN FETCH dp.user u WHERE u.isActive = true")
    List<DoctorProfile> findAllActiveDoctors();

//    @Query("SELECT dp FROM DoctorProfile dp JOIN FETCH dp.user u " +
//            "WHERE LOWER(dp.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')) AND u.isActive = true")
//    List<DoctorProfile> findBySpecialtyContainingIgnoreCase(@Param("specialty") String specialty);
}
