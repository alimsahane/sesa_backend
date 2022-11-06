package com.sesa.medical.users.services.imp;

import com.sesa.medical.security.TokenProvider;
import com.sesa.medical.security.dto.UserEditPasswordDto;
import com.sesa.medical.users.entities.*;
import com.sesa.medical.users.repository.IAdresseRepo;
import com.sesa.medical.users.repository.IOldPasswordRepo;
import com.sesa.medical.users.repository.IStatusUsersRepository;
import com.sesa.medical.users.repository.IUsersRepository;
import com.sesa.medical.users.services.IUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements IUserService {
    @Autowired
    IUsersRepository iUsersRepository;
    @Autowired
    IUsersRepository userRepo;
    @Autowired
    IStatusUsersRepository statusRepo;
    @Autowired
    IOldPasswordRepo oldPasswordRepo;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    IAdresseRepo adresseRepo;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private ResourceBundleMessageSource messageSource;
    @Override
    public Page<Users> getAllUsers(int page, int size, String sort) {
        Sort sorted = Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page,size,sorted);
        return userRepo.findAll(pageable);
    }

    @Override
    public Users getUsernameOrEmailOrTel1(String username, String email, String tel1) {
        Optional<Users> users = userRepo.findByUsernameOrEmailOrTel1(username, email, tel1);
        if (!users.isPresent() || users.get().isDelete()) {
            throw new ResourceNotFoundException("User  not found");
        }
        return users.get();
    }

    @Override
    public Users getById(Long id) {
        Optional<Users> user = userRepo.findById(id);
        if (!user.isPresent() || user.get().isDelete()) {
            throw new ResourceNotFoundException("User id " + id + " not found");
        }
        return user.get();
    }

    @Override
    public Users getByUsername(String username) {
        Optional<Users> user = userRepo.findByUsernameIgnoreCase(username);
        if (!user.isPresent() || user.get().isDelete()) {
            throw new ResourceNotFoundException("User " + username + " not found");
        }
        return user.get();
    }

    @Override
    public Users editPassword(Users user, UserEditPasswordDto u) {
        OldPassword oldPassword = oldPasswordRepo.save(new OldPassword(encoder.encode(u.getPassword())));
        user.getOldPasswords().add(oldPassword);
        user.setPassword(encoder.encode(u.getPassword()));
     Users users =   userRepo.save(user);
        return users;
    }

    @Override
    public Users getByEmail(String email) {
        Optional<Users> user = userRepo.findByEmail(email);
        if (!user.isPresent() || user.get().isDelete()) {
            throw new ResourceNotFoundException("User email" + email + " not found");
        }
        return user.get();
    }

    @Override
    public Optional<Users> getByTel(String tel) {
        Optional<Users> user = userRepo.findByTel1(tel);
        if (!user.isPresent()) {
          user = userRepo.findByTel2(tel);
          if(!user.isPresent()) {
              throw new ResourceNotFoundException("User phone: " + tel + " not found");
          }
        }
        return user;
    }

    @Override
    public Users resetPassword(Users user, String password) {
        user.setPassword(encoder.encode(password));
        OldPassword oldPassword = oldPasswordRepo.save(new OldPassword(encoder.encode(password)));
        user.getOldPasswords().add(oldPassword);
        user.setOtpCode(null);
        user.setOtpCreatedAt(null);
        user.setTokenAuth(null);
        return userRepo.save(user);
    }

    @Override
    public Users checkUserAndGenerateCode(String login) {
        Users user;
        if (login.contains("@")) {
            user = userRepo.findByEmail(login).orElseThrow(() -> new ResourceNotFoundException(
                    messageSource.getMessage("messages.user_not_found-email", null, LocaleContextHolder.getLocale())));
            String token = tokenProvider.createTokenLocalUser(user,true);
            user.setTokenAuth(token);
        } else {
            user = getByTel(login).orElseThrow(() -> new ResourceNotFoundException(
                    messageSource.getMessage("messages.user_not_found-phone", null, LocaleContextHolder.getLocale())));
            int codeOtp = tokenProvider.generateOtpCode();
            user.setOtpCode(String.valueOf(codeOtp));
            user.setOtpCreatedAt(LocalDateTime.now());
        }
        return userRepo.save(user);
    }

    @Override
    public Users lockAndUnlockUsers(Long id_user, boolean status) {
        Users u = getById(id_user);
        if(status == true) {
             Optional<StatusUsers> statusUser = statusRepo.findByName(EStatusUser.USER_ENABLED);
            u.setStatus(statusUser.get());
        } else {
            Optional<StatusUsers> statusUser = statusRepo.findByName(EStatusUser.USER_DISABLED);
            u.setStatus(statusUser.get());
        }
        return  userRepo.save(u);
    }

    @Override
    public Users updateFcmToken(String fcmToken,Long userId) {
        Users users = getById(userId);
        users.setFcmToken(fcmToken);
        return userRepo.save(users);
    }


    @Override
    public Users getNewCodeValidationEmail(Long id) {
        return null;
    }


    @Override
    public Users editToken(Long id, String token) {
        Users user = getById(id);
        user.setTokenAuth(token);
        return userRepo.save(user);
    }


    @Override
    public Users editStatus(Long id, Long statusId) {
        Users user = getById(id);
        StatusUsers status = statusRepo.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status id " + id + " not found"));
        user.setStatus(status);
        if (user.getStatus().equals(status)) {
            return user;
        }
        return userRepo.save(user);
    }

    @Override
    public Users editEmail(Long id, String email) {
        Users user = getById(id);
        user.setEmail(email);
        return userRepo.save(user);
    }

    @Override
    public boolean existsByEmail(String email, Long id) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        Optional<Users> user = userRepo.findByEmail(email);
        return checkOwnerIdentity(id, user);
    }

    @Override
    public boolean existsByUsername(String username, Long id) {
        Optional<Users> user = userRepo.findByUsernameIgnoreCase(username);
        return checkOwnerIdentity(id, user);
    }

    @Override
    public boolean existsByTel(String tel, Long id) {
        if (tel == null ) {
            return false;
        }
        Optional<Users> user = userRepo.findByTel1(tel);
        return checkOwnerIdentity(id, user);
    }

    @Override
    public boolean existsByTel2(String tel, Long id) {
        if (tel == null ) {
            return false;
        }
        Optional<Users> user = userRepo.findByTel2(tel);
        return checkOwnerIdentity(id, user);
    }

    @Override
    public Users updateAuthToken(Long id, String token) {
        Optional<Users> users = userRepo.findById(id);
        if(!users.isPresent() || users.get().isDelete()) {
            throw new ResourceNotFoundException("User  not found");
        }
        users.get().setTokenAuth(token);
        return userRepo.save(users.get());
    }

    @Override
    public Users updateOtpCode(Long id, String code) {
        Optional<Users> users = userRepo.findById(id);
        if(!users.isPresent() || users.get().isDelete()) {
            throw new ResourceNotFoundException("User  not found");
        }
        users.get().setOtpCode(code);
        users.get().setOtpCreatedAt(LocalDateTime.now());
        return userRepo.save(users.get());
    }

    @Override
    public void changeStatusEmailVerify(Users users) {
      users.setEmailVerify(true);
      userRepo.save(users);
    }

    @Override
    public void changeStatusPhoneVerify(Users users) {
          users.setPhoneVerify(true);
          userRepo.save(users);
    }

    private boolean checkOwnerIdentity(Long id, Optional<Users> user) {
        boolean taken = false;
        if (!user.isPresent()) {
            return taken;
        }
        if (id != null) {
            if (id.equals(user.get().getUserId())) {
                return taken;
            } else {
                taken = true;
                return taken;
            }
        } else {
            taken = true;
            return taken;
        }
    }
    @Override
    public Users deleteUser(Long userId) {
        Users user = getById(userId);
        user.setDelete(true);
        return userRepo.save(user);
    }

    @Override
    public Users updateProfil(Users user,Long id) {
        Users u = userRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("Users where id: " + id + "not found"));
             u.setFirstName(user.getFirstName());
             u.setLastName(user.getLastName());
             u.setTel2(user.getTel2());
             u.setSexe(user.getSexe());
             u.setBirthdate(user.getBirthdate());
             u.setBirthdatePlace(user.getBirthdatePlace());
             u.setNationality(user.getNationality());
             u.setMaritalStatus(user.getMaritalStatus());
             u.setUpdateAt(LocalDateTime.now());
        return userRepo.save(u);
    }

    @Override
    public Adresses createAdresseUser(Adresses a,Long id) {
        Users u = userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Users where id: "  + id + " not exist"));
        if(u.getAdresses() != null) {
            throw  new RuntimeException("this user already has an address");
        }
        a.setUsers(u);
        return adresseRepo.save(a);
    }

    @Override
    public Adresses updateAdresseUser(Adresses ad, Long id) {
        Adresses a = adresseRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Adresse where id: "  + id + " not exist"));
        a.setCountry(ad.getCountry());
        a.setPostalCode(ad.getPostalCode());
        a.setQuater(ad.getQuater());
        a.setStreet(ad.getStreet());
        a.setTown(ad.getTown());
        a.setLatitude(ad.getLatitude());
        a.setLongitude(ad.getLongitude());
        return adresseRepo.save(a);
    }

    @Override
    public Adresses getAdresseUser(Long id) {
        Users u = getById(id);
        Adresses adresses = adresseRepo.findByUsers(u);
        return adresses;
    }
}
