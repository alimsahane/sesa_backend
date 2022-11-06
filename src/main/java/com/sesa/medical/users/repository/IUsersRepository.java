package com.sesa.medical.users.repository;

import com.sesa.medical.users.entities.RolesUser;
import com.sesa.medical.users.entities.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IUsersRepository extends JpaRepository<Users,Long> {
    Optional<Users> findByUsernameIgnoreCase(String username);

    Optional<Users> findByUsername(String username);
    Optional<Users> findByUsernameOrEmailOrTel1(String username,String email,String tel1);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByTel1(String tel);

    Optional<Users> findByTel2(String tel2);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Users> findByTokenAuth(String code);

    Page<Users> findByIsDelete(boolean b, Pageable p);

    Page<Users> findDistinctByIsDeleteFalseAndRolesIn(List<RolesUser> roles, Pageable p);

    Page<Users> findDistinctByIsDeleteFalseAndRolesNotIn(List<RolesUser> rolesManagers, Pageable p);
}
