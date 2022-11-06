package com.sesa.medical.users.repository;

import com.sesa.medical.users.entities.OldPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOldPasswordRepo extends JpaRepository<OldPassword,Long> {

}
