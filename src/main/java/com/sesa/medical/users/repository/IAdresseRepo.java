package com.sesa.medical.users.repository;

import com.sesa.medical.users.entities.Adresses;
import com.sesa.medical.users.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAdresseRepo extends JpaRepository<Adresses, Long> {
    Adresses findByUsers(Users u);
}
