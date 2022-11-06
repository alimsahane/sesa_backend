package com.sesa.medical.users.repository;

import com.sesa.medical.users.entities.ERoles;
import com.sesa.medical.users.entities.RolesUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface IRolesRepository extends JpaRepository<RolesUser,Long> {
    Optional<RolesUser> findByName(ERoles roles);
}
