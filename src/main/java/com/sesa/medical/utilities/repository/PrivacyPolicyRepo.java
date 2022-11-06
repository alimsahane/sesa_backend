package com.sesa.medical.utilities.repository;

import com.sesa.medical.utilities.entities.PrivacyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivacyPolicyRepo extends JpaRepository<PrivacyPolicy,Long> {
}
