package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.InsurancePolicy;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides a set of search methods from Spring Boot for InsurancePolicy data.
 */
@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, UUID> {
  InsurancePolicy findInsurancePolicyByPatientsId(UUID patientId);
}
