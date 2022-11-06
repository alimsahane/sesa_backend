package com.sesa.medical.prestation.repositoty;

import com.sesa.medical.prestation.entities.DescriptionPrestation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DescriptionPrestaRepo extends JpaRepository<DescriptionPrestation,Long> {
}
