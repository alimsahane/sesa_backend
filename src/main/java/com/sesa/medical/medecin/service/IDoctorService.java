package com.sesa.medical.medecin.service;

import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.patient.entities.Patient;
import com.sesa.medical.users.entities.Users;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IDoctorService {
   Doctors saveDoctor(Users u);

   Page<Users> getAllDoctorPagination(int page, int size, String sort);

   Doctors addDoctorToHospital(Long id_doc, Long idhospital);

   void activeAndDesactiveDoctore(Long doctorId,boolean status);

   Map<String, Object> updatePasswordDoctorAndSendEmail(Doctors doctors);



}
