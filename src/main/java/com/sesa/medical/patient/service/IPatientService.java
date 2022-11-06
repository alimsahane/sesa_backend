package com.sesa.medical.patient.service;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.patient.dto.ChatDto;
import com.sesa.medical.patient.entities.*;
import com.sesa.medical.sos.entities.Sos;
import com.sesa.medical.users.entities.Users;
import org.springframework.data.domain.Page;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.SortedSet;

public interface IPatientService {
    Patient savePatient(Users p);
    Page<Patient> getListPaginatePatient(int page,int size, String sort,String SortOrder);
    Patient getOnePatient(Long id);
    Abonnement findAbonnementByPatient(Patient p);
    List<Doctors> findDoctorByHospitalAndSosreceiveTrue(Hospitals hospitals);
    boolean checkInformationOfPatient(Patient p);
    TemplateSos getOneTemplate();
    Parametre getParametrePatient(Patient patient);
    Carnet createCarnet(Long id_patient);
    Parametre createParametre(Parametre param, Long id_carnet);
    Sos CreateSosPatient(Sos sos, Long id_patient,Long id_medecin);
    Parametre createParametrePatient(Parametre param, Long id_patient);
    Page<Parametre> getParametreOfPatient(Long id_patient,int page, int size, String sort, String sortOrder);
    Parametre updateParametre(Parametre param, Long id_param);
    void deleteParametre(Long id_param);
    ModePay createModePay(ModePay modePay);
    ModePay updateModePay(ModePay modePay,Long id_mode);
    void deleteModePay(Long id_mode);
    ModePay getOneMode(Long id_mod);

    Chat sendMessage(Chat chat);

    List<Chat> updateStatusOfAllMessage(Long receiverId,Long senderId);

    int countAllMessageWhereStatusWrite(Long receiverId,Long senderId);


   // Page<Chat> getListMessageUser(int page,int size, String sort,String sortOder,Long userId);

    List<Chat> getListMessageUser(Long userId);

   // Page<Chat> getListMessageUser(int page,int size, String sort,String sortOder,Long userId,Long senderId);
  List<Chat> getListMessageUser(Long receiverId,Long senderId);

    Abonnement getOneAbonnement(Long id_abo);

    Abonnement createAbonnement(Abonnement abonnement, Long patientId,Long hospitalId,Long packId);

    Payement createPayement(Payement pay,Long aboId,Long modeId);

    Payement updatePayementAndAbonnement(String transactionId);

    Patient changeStatusAccountToMember(Long patientId, EStatusAccount statusAccount);

    List<Abonnement> getAllAbonnementPatient(Long patientId);

    Chat sendImageFromChat(String imageUrl,Long senderId,Long receiverId);

}
