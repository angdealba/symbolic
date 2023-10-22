package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalPractitionerRepository extends JpaRepository<MedicalPractitioner, Long> {
}
