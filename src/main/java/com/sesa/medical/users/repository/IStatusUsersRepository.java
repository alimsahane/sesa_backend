package com.sesa.medical.users.repository;

import com.sesa.medical.users.entities.EStatusUser;
import com.sesa.medical.users.entities.StatusUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IStatusUsersRepository extends JpaRepository<StatusUsers,Long> {
    Optional<StatusUsers> findByName(EStatusUser statusUser);
}
