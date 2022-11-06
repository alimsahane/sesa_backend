package com.sesa.medical.hopital.repository;

import com.sesa.medical.hopital.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDepartmentRepo extends JpaRepository<Department,Long> {
}
