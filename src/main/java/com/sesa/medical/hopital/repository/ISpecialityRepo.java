package com.sesa.medical.hopital.repository;

import com.sesa.medical.hopital.entities.Specialitys;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISpecialityRepo extends JpaRepository<Specialitys, Long> {
}
