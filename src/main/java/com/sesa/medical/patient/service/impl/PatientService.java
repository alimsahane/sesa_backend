package com.sesa.medical.patient.service.impl;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.hopital.repository.IHospitalRepo;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.medecin.repository.IDoctorsRepo;
import com.sesa.medical.packsesa.entities.PackSesa;
import com.sesa.medical.packsesa.service.IPackSesaService;
import com.sesa.medical.patient.dto.ChatDto;
import com.sesa.medical.patient.entities.*;
import com.sesa.medical.patient.repository.*;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.security.TokenProvider;
import com.sesa.medical.security.dto.AuthProvider;
import com.sesa.medical.security.jwt.JwtUtils;
import com.sesa.medical.sos.entities.Sos;
import com.sesa.medical.sos.repository.ISosRepo;
import com.sesa.medical.users.entities.*;
import com.sesa.medical.users.repository.IRolesRepository;
import com.sesa.medical.users.repository.IStatusUsersRepository;
import com.sesa.medical.users.repository.IUsersRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PatientService implements IPatientService {

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    IRolesRepository rolesRepository;

    @Autowired
    private IStatusUsersRepository statusRepo;

    @Autowired
    TokenProvider tokenProvider;
    @Autowired
    IPatientRepo patientRepo;
    @Autowired
    IDoctorsRepo doctorsRepo;
    @Autowired
    IHospitalRepo hospitalRepo;
    @Autowired
    IAbonnementRepo abonnementRepo;
    @Autowired
    ResourceBundleMessageSource message;
    @Autowired
    ISosRepo sosRepo;
    @Autowired
    ItemplateSosRepo itemplateSosRepo;
    @Autowired
    IParametreRepo parametreRepo;
    @Autowired
    ICarnetRepo carnetRepo;
    @Autowired
    IModeRepo modeRepo;

    @Autowired
    IChatRepo chatRepo;

    @Autowired
    IUsersRepository usersRepository;

    @Autowired
    IPackSesaService packSesaService;

    @Autowired
    IPayementRepo payementRepo;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    IStatusAccountRepository statusAccountRepo;

    @Override
    public Patient savePatient(Users p) {
        Patient patient= new Patient(p.getUsername(), p.getEmail(), encoder.encode(p.getPassword()));
        patient.setTel1(p.getTel1());
        String matricule = "PA" + RandomStringUtils.random(9, 35, 125, true, true, null, new SecureRandom());
        patient.setMatricule(matricule);
        Set<RolesUser> roles = new HashSet<>();
            RolesUser rolesUser = rolesRepository.findByName(ERoles.ROLE_USER).orElseThrow(()-> new ResourceNotFoundException("Role:  "  +  ERoles.ROLE_USER.name() +  "  not found"));
            roles.add(rolesUser);
        patient.setRoles(roles);
        StatusUsers status = statusRepo.findByName(EStatusUser.USER_DISABLED)
                .orElseThrow(() -> new ResourceNotFoundException("Status: " + EStatusUser.USER_DISABLED + " not found"));
        patient.setStatus(status);
        StatusAccount statusAccount = statusAccountRepo.findByName(EStatusAccount.STANDARD);
        patient.setProvider(AuthProvider.valueOf(p.getProviderName()));
        patient.setProviderId(String.valueOf(AuthProvider.local.ordinal() + 1));
        patient.setCreatedAt(LocalDateTime.now());
        patient.setStatusAccount(statusAccount);
        Patient patient1 = patientRepo.save(patient);
        if(patient1.getCarnet() == null)  {
            createCarnet(patient.getUserId());
        }
        return  patient1;
    }

    @Override
    public Page<Patient> getListPaginatePatient(int page, int size, String sort, String sortOrder) {
        Sort sort1 = Sort.by(Sort.Direction.fromString(sortOrder),sort);
        Pageable pageable = PageRequest.of(page,size,sort1);
        return patientRepo.findAll(pageable);
    }

    @Override
    public Patient getOnePatient(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Patient :" + id + "not found"));
        return patient;
    }

    @Override
    public Abonnement findAbonnementByPatient(Patient p) {
       Optional<Abonnement> ab = abonnementRepo.findByPatientAndEtatTrue(p);
       if(ab.get() == null) {
           throw  new ResourceNotFoundException(message.getMessage("messages.abonnement_not_exists", null, LocaleContextHolder.getLocale()));
       }
       return ab.get();
    }

    @Override
    public List<Doctors> findDoctorByHospitalAndSosreceiveTrue(Hospitals hospitals) {
        List<Doctors> doctors = doctorsRepo.findDistinctByHospitalsAndSosReceivedTrue(hospitals);
        if(doctors.size() == 0) {
            throw new ResourceNotFoundException(message.getMessage("messages.doctors_not_received_sos",null,LocaleContextHolder.getLocale()));
        }
        return doctors;
    }


    @Override
    public boolean checkInformationOfPatient(Patient p) {
        boolean status = false;
        if(p.getCarnet() == null) {
            status = false;
            throw  new ResourceNotFoundException(message.getMessage("messages.carnet_not_exists", null, LocaleContextHolder.getLocale()));
        } else {
            if(p.getCarnet().getParametres().size() <= 0) {
                status = false;
                throw  new ResourceNotFoundException(message.getMessage("messages.paramêtre_not_exists", null, LocaleContextHolder.getLocale()));
            } else {
                status = true;
            }
        }
        return status;
    }
    @Override
   public TemplateSos getOneTemplate(){
        return itemplateSosRepo.findByEtatTrue();
    }

    @Override
    public Parametre getParametrePatient(Patient patient) {
        Parametre param = parametreRepo.findByCarnetAndEtatTrue(patient.getCarnet());
        if(param == null) {
            throw new ResourceNotFoundException("le  patient: " + patient.getUserId() +" ne dispose pas de parametre");
        }
        return param;
    }

    @Override
    public Carnet createCarnet(Long id_patient) {
        Patient p = getOnePatient(id_patient);
        Carnet carnet = new Carnet();
        carnet.setPatient(p);
        carnet.setCreatedAt(LocalDate.now());
        carnet.setCode("SESA_" + RandomStringUtils.random(5, 35, 125, true, true, null, new SecureRandom()));
        return carnetRepo.save(carnet);
    }

    @Override
    public Parametre createParametre(Parametre param, Long id_carnet) {
        Carnet carnet = carnetRepo.findById(id_carnet).orElseThrow(()-> new ResourceNotFoundException("carnet : " + id_carnet+ " not found"));
        param.setCarnet(carnet);
        param.setEtat(true);
        param.setCreatedAt(LocalDateTime.now());
       /* Parametre paraupdate = parametreRepo.findByEtatTrue();
        paraupdate.setEtat(false);
        parametreRepo.save(paraupdate);*/
        return parametreRepo.save(param);
    }

    @Override
    public Sos CreateSosPatient(Sos sos, Long id_patient,Long id_medecin) {
        Patient pat = getOnePatient(id_patient);
        Doctors doc = doctorsRepo.findById(id_medecin).orElseThrow(() -> new ResourceNotFoundException("doctor where id: "+ id_medecin+ " not found"));
        sos.setPatient(pat);
        sos.setDoctors(doc);
        sos.setCreatedAt(LocalDateTime.now());
        return sosRepo.save(sos);
    }

    @Override
    public Parametre createParametrePatient(Parametre param, Long id_patient) {
        Patient patient = getOnePatient(id_patient);
        patient.getCarnet().getParametres().forEach(p -> {
            p.setEtat(false);
            parametreRepo.save(p);
        });
        Parametre pa =  createParametre(param,patient.getCarnet().getId());
        return pa;
    }

    @Override
    public Page<Parametre> getParametreOfPatient(Long id_patient,int page, int size, String sort, String sortOrder) {
        Patient patient = getOnePatient(id_patient);
       Carnet carnet = carnetRepo.findByPatient(patient);
        Sort sorted = Sort.by(Sort.Direction.fromString(sortOrder), sort);
        Pageable page1 = PageRequest.of(page,size,sorted);
        return parametreRepo.findByCarnet(carnet,page1);
    }

    @Override
    public Parametre updateParametre(Parametre param, Long id_param) {
        Parametre para = parametreRepo.findById(id_param).orElseThrow(()-> new ResourceNotFoundException("Parameter where id: "+ id_param + " not found"));
         para.setPerimetreBranchial(param.getPerimetreBranchial());
         para.setTemperature(param.getTemperature());
         para.setUpdateAd(param.getUpdateAd());
         para.setFrequenceCardiaque(param.getFrequenceCardiaque());
         para.setPouls(param.getPouls());
         para.setPoids(param.getPoids());
         para.setFrequenceRespiratoire(param.getFrequenceRespiratoire());
         para.setSaturationOxygene(param.getSaturationOxygene());
         para.setTaille(param.getTaille());
         return  parametreRepo.save(param);
    }

    @Override
    public void deleteParametre(Long id_param) {
        Parametre parametre = parametreRepo.findById(id_param).orElseThrow(() -> new ResourceNotFoundException("Parameter where id: "+ id_param + " not found"));
          if(parametre.isEtat() == true) {
              throw  new RuntimeException(message.getMessage("messages.paramêtre_not_delete", null, LocaleContextHolder.getLocale()));
          }
          parametreRepo.delete(parametre);
    }

    @Override
    public ModePay createModePay(ModePay modePay) {
        return modeRepo.save(modePay);
    }

    @Override
    public ModePay updateModePay(ModePay modePay, Long id_mode) {
        ModePay mode = getOneMode(id_mode);
        if(mode.getName().equals(modePay.getName())) {
            mode.setEtat(!mode.isEtat());
        }
        mode.setName(modePay.getName());
        mode.setUpdateAt(new Date());
        return mode;
    }

    @Override
    public void deleteModePay(Long id_mode) {
        ModePay mode = getOneMode(id_mode);
        mode.setDelete(true);
        modeRepo.save(mode);
    }

    @Override
    public ModePay getOneMode(Long id_mod) {
        ModePay mode = modeRepo.findById(id_mod).orElseThrow(()-> new ResourceNotFoundException("Payement mode where id: "+id_mod+" not found"));
        return mode;
    }

    @Override
    public Chat sendMessage(Chat chat) {
        return chatRepo.save(chat);
    }

    @Override
    public List<Chat> updateStatusOfAllMessage(Long receiverId, Long senderId) {
        List<Chat> list = new ArrayList<>();
        Users users = usersRepository.findById(receiverId).orElseThrow(()-> new ResourceNotFoundException("Receiver where id: "+receiverId+" not found"));
        Users sender = usersRepository.findById(senderId).orElseThrow(()-> new ResourceNotFoundException("Sender where id: "+senderId+" not found"));
        List<Chat> chat = chatRepo.findByReceiverAndSenderAndStatusMessage(users,sender,EStatusMessage.USER_WRITE);
        chat.forEach(c -> {
           c.setStatusMessage(EStatusMessage.USER_READ);
        Chat chat1 =   chatRepo.save(c);
           list.add(chat1);
       });
        return list;
    }

    @Override
    public int countAllMessageWhereStatusWrite(Long receiverId, Long senderId) {
        Users users = usersRepository.findById(receiverId).orElseThrow(()-> new ResourceNotFoundException("Receiver where id: "+receiverId+" not found"));
        Users sender = usersRepository.findById(senderId).orElseThrow(()-> new ResourceNotFoundException("Sender where id: "+senderId+" not found"));
        return chatRepo.countChatByReceiverAndSenderAndStatusMessage(users,sender,EStatusMessage.USER_WRITE);
    }

    /*@Override
    public Page<Chat> getListMessageUser(int page, int size, String sort, String sortOder, Long userId) {
        Users users = usersRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("Receiver  where id: "+userId+" not found"));
        return chatRepo.getAllSender(users,PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOder), sort)));
    }*/

    @Override
    public List<Chat> getListMessageUser(Long userId) {
        Users users = usersRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("Receiver  where id: "+userId+" not found"));
        Query query =  entityManager.createNativeQuery("SELECT distinct  on(c.sender_id) id,c.sender_id as sender_id,c.receiver_id as receiver_id,c.message as message,c.message_type as message_type,c.status_message as status_message,c.created_at as created_at,c.file_url as file_url FROM  chat c  where c.receiver_id="+userId+" order by c.sender_id ,c.id desc",Chat.class);

        return query.getResultList();
    }

   /* @Override
    public Page<Chat> getListMessageUser(int page,int size,String sort,String sortOrder,Long id_user,Long senderid) {
        Users users = usersRepository.findById(id_user).orElseThrow(()-> new ResourceNotFoundException("Receiver where id: "+id_user+" not found"));
        Users sender = usersRepository.findById(senderid).orElseThrow(()-> new ResourceNotFoundException("Sender where id: "+id_user+" not found"));
        return chatRepo.findByReceiverAndSender(users,sender,PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sort)));
    }*/
    @Override
    public List<Chat> getListMessageUser(Long receiverId,Long senderid) {
        Users users = usersRepository.findById(receiverId).orElseThrow(()-> new ResourceNotFoundException("Receiver where id: "+receiverId+" not found"));
        Users sender = usersRepository.findById(senderid).orElseThrow(()-> new ResourceNotFoundException("Sender where id: "+senderid+" not found"));
        return chatRepo.findByReceiverAndSender(users,sender);
    }
    @Override
    public Abonnement getOneAbonnement(Long id_abo) {
        return abonnementRepo.findById(id_abo).orElseThrow(() -> new ResourceNotFoundException("Abonnement where id: "+id_abo+" not found"));
    }

    @Override
    public Abonnement createAbonnement(Abonnement abonnement, Long patientId, Long hospitalId, Long packId) {
        Patient patient = getOnePatient(patientId);
        Hospitals hospitals = hospitalRepo.findById(hospitalId).orElseThrow(()-> new ResourceNotFoundException("Hospital  where id: "+hospitalId+" not found"));;
        PackSesa packSesa = packSesaService.getById(packId);
        abonnement.setPackSesa(packSesa);
        abonnement.setPatient(patient);
        abonnement.setHospitals(hospitals);
        return abonnementRepo.save(abonnement);
    }

    @Override
    public Payement createPayement(Payement pay, Long aboId, Long modeId) {
        ModePay modePay = modeRepo.getOne(modeId);
        Abonnement abonnement = getOneAbonnement(aboId);
        pay.setAbonnement(abonnement);
        pay.setModePay(modePay);
        return payementRepo.save(pay);
    }

    @Override
    public Payement updatePayementAndAbonnement(String transactionId) {
        Payement payement = payementRepo.findByIdTransaction(transactionId);
        if(payement == null) {
            new ResourceNotFoundException("payement  where transactionId: "+transactionId+" not exist");
        }
        payement.setEtat(true);
        Abonnement abonnement = payement.getAbonnement();
        abonnement.setEtat(true);
        abonnementRepo.save(abonnement);
        return payementRepo.save(payement);
    }

    @Override
    public Patient changeStatusAccountToMember(Long patientId, EStatusAccount statusAccount) {
        Patient part = getOnePatient(patientId);
        StatusAccount status = statusAccountRepo.findByName(statusAccount);
        part.setStatusAccount(status);
        return patientRepo.save(part);
    }

    @Override
    public List<Abonnement> getAllAbonnementPatient(Long patientId) {
        Patient patient = getOnePatient(patientId);
        return abonnementRepo.findByPatient(patient);
    }

    @Override
    public Chat sendImageFromChat(String imageUrl, Long senderId, Long receiverId) {
        Users receiver = usersRepository.findById(receiverId).orElseThrow(()-> new ResourceNotFoundException("Receiver where id: "+receiverId+" not found"));
        Users sender = usersRepository.findById(senderId).orElseThrow(()-> new ResourceNotFoundException("Sender where id: "+senderId+" not found"));
        Chat chat = new Chat();
        chat.setCreatedAt(LocalDateTime.now());
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setMessageType(EmessageType.document);
        chat.setStatusMessage(EStatusMessage.USER_WRITE);
        chat.setFileUrl(imageUrl);
    return chatRepo.save(chat);
    }


}
