package com.sesa.medical.users.services;

import com.sesa.medical.security.dto.UserEditPasswordDto;
import com.sesa.medical.users.entities.Adresses;
import com.sesa.medical.users.entities.Users;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserService {
    Page<Users> getAllUsers(int page, int size, String sort);

    Users getUsernameOrEmailOrTel1(String username, String email, String tel1);

    boolean existsByEmail(String email, Long id);

    boolean existsByUsername(String username, Long id);

    Users getById(Long id);

    Users getByUsername(String username);

    Users editPassword(Users user, UserEditPasswordDto u);

    Users getByEmail(String email);

    Optional<Users> getByTel(String tel);

    Users resetPassword(Users user, String password);

    Users getNewCodeValidationEmail(Long id);

    Users editToken(Long id, String token);

    Users editEmail(Long id, String email);

    Users editStatus(Long id, Long statusID);

    boolean existsByTel(String tel, Long id);

    boolean existsByTel2(String tel, Long id);

    Users updateAuthToken(Long id, String token);

    Users updateOtpCode(Long id, String code);

    void changeStatusEmailVerify(Users users);

    void changeStatusPhoneVerify(Users users);

    Users deleteUser(Long userId);

    Users updateProfil(Users users, Long id);

    Adresses createAdresseUser(Adresses a, Long id);

    Adresses updateAdresseUser(Adresses a, Long id);

    Adresses getAdresseUser(Long id);

    Users checkUserAndGenerateCode(String login);

    Users lockAndUnlockUsers(Long id_user, boolean status);

    Users updateFcmToken(String fcmToken, Long userId);
}
