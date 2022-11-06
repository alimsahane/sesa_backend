package com.sesa.medical.sos.repository;

import com.sesa.medical.sos.entities.Sos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISosRepo extends JpaRepository<Sos,Long> {
}
