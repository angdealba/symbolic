package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.InsurancePolicy;
import com.symbolic.symbolic.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {
    InsurancePolicy findInsurancePolicyByPatientsId(Long patientId);
}
