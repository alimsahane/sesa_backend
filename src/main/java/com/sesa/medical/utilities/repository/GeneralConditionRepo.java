package com.sesa.medical.utilities.repository;

import com.sesa.medical.utilities.entities.GeneralCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralConditionRepo  extends JpaRepository<GeneralCondition,Long> {
}
