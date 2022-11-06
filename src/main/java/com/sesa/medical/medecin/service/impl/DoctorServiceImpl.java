package com.sesa.medical.medecin.service.impl;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.hopital.repository.IHospitalRepo;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.medecin.repository.IDoctorsRepo;
import com.sesa.medical.medecin.service.IDoctorService;
import com.sesa.medical.security.TokenProvider;
import com.sesa.medical.security.dto.AuthProvider;
import com.sesa.medical.security.jwt.JwtUtils;
import com.sesa.medical.users.entities.*;
import com.sesa.medical.users.repository.IRolesRepository;
import com.sesa.medical.users.repository.IStatusUsersRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorServiceImpl implements IDoctorService {
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    IDoctorsRepo doctorsRepo;

    @Autowired
    IRolesRepository rolesRepository;

    @Autowired
    private IStatusUsersRepository statusRepo;

    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    IHospitalRepo hospitalRepo;

    @Override
    public Doctors saveDoctor(Users d) {
        Doctors doctor= new Doctors(d.getUsername(), d.getEmail(), encoder.encode(d.getPassword()));
        doctor.setTel1(d.getTel1());
        doctor.setIsActive(true);
        String matricule = "Sesa" + RandomStringUtils.random(9, 35, 125, true, true, null, new SecureRandom());
        doctor.setMatricule(matricule);
        Set<RolesUser> rolesList = new HashSet<>();
        List<RolesUser> rolesUser = rolesRepository.findAll();
        rolesUser.forEach(r -> {
            if(r.getId() == 1L || r.getId() == 3L) {
                rolesList.add(r);
            }
        });
     //   rolesList.add(rolesRepository.findByName(ERoles.ROLE_MEDECIN).get());
     //   rolesList.add(rolesRepository.findByName(ERoles.ROLE_USER).get());
       /* d.getRoleNames().forEach( roleName -> {
            RolesUser rolesUser = rolesRepository.findByName(roleName).orElseThrow(()-> new ResourceNotFoundException("Role: " + roleName + " not found"));
           rolesList.add(rolesUser);
        });*/
        doctor.setRoles(rolesList);
        StatusUsers status = statusRepo.findByName(EStatusUser.USER_DISABLED)
                .orElseThrow(() -> new ResourceNotFoundException("Status: " + EStatusUser.USER_DISABLED + " not found"));
        doctor.setStatus(status);
        doctor.setProvider(AuthProvider.valueOf(d.getProviderName()));
        doctor.setProviderId(String.valueOf(AuthProvider.local.ordinal() + 1));
        doctor.setCreatedAt(LocalDateTime.now());
        return doctorsRepo.save(doctor);
    }

    @Override
    public Page<Users> getAllDoctorPagination(int page, int size, String sort) {
        Sort sorted = Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page,size);
        return doctorsRepo.findDistinctByIsDeleteFalseOrderByIsActiveDesc(pageable);
    }

    @Override
    public Doctors addDoctorToHospital(Long id_doc, Long idhospital) {
        Doctors doctors = doctorsRepo.findById(id_doc).orElseThrow(() -> new ResourceNotFoundException("Doctor where id: "+id_doc+" not found"));
        Hospitals hospitals = hospitalRepo.findById(idhospital).orElseThrow(() -> new ResourceNotFoundException("Hospital where id: "+id_doc+" not found"));
        doctors.setHospitals(hospitals);
        return doctorsRepo.save(doctors);
    }

    @Override
    public void activeAndDesactiveDoctore(Long doctorId, boolean status) {
        Doctors doctors = doctorsRepo.findById(doctorId).orElseThrow(()-> new ResourceNotFoundException("Doctor where id: "+doctorId+" not found"));
        doctors.setIsActive(status);
        doctorsRepo.save(doctors);
    }

    @Override
    public Map<String, Object> updatePasswordDoctorAndSendEmail(Doctors doctors) {
        String password = RandomStringUtils.random(15, 35, 125, true, true, null, new SecureRandom());
        doctors.setPassword(encoder.encode(password));
        Doctors users = doctorsRepo.save(doctors);
        Map<String, Object> userAndPasswordNotEncoded = new HashMap<>();
        userAndPasswordNotEncoded.put("users", users);
        userAndPasswordNotEncoded.put("password", password);
        return userAndPasswordNotEncoded;
    }
}
