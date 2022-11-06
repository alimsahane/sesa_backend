package com.sesa.medical.seeder;

import com.sesa.medical.hopital.entities.Hospitals;
import com.sesa.medical.hopital.repository.IHospitalRepo;
import com.sesa.medical.hopital.service.IHospitalService;
import com.sesa.medical.medecin.entities.Doctors;
import com.sesa.medical.medecin.repository.IDoctorsRepo;
import com.sesa.medical.medecin.service.IDoctorService;
import com.sesa.medical.patient.entities.*;
import com.sesa.medical.patient.repository.IAbonnementRepo;
import com.sesa.medical.patient.repository.ICarnetRepo;
import com.sesa.medical.patient.repository.IPatientRepo;
import com.sesa.medical.patient.repository.ItemplateSosRepo;
import com.sesa.medical.patient.service.IPatientService;
import com.sesa.medical.security.dto.AuthProvider;
import com.sesa.medical.users.entities.*;
import com.sesa.medical.users.repository.IRolesRepository;
import com.sesa.medical.users.repository.IStatusUsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class DatabaseSeeder {

    @Autowired
    IRolesRepository rolesRepository;
    @Autowired
    IStatusUsersRepository statusUsersRepository;
    @Autowired
    IHospitalRepo hospitalRepo;
    @Autowired
    IPatientService patientService;
    @Autowired
    IAbonnementRepo abonnementRepo;
    @Autowired
    IPatientRepo patientRepo;
    @Autowired
    IDoctorsRepo doctorsRepo;
    @Autowired
    IDoctorService doctorService;
    @Autowired
    ICarnetRepo carnetRepo;
    @Autowired
    ItemplateSosRepo templateSosRepo;


    @EventListener
    public void seed(ContextRefreshedEvent event) {
        seedRoleUsersTable();
        seedStatusUsersTable();
        seedHospitalsTable();
        createOneDoctorsToHospital();
        createPatientSetAbonnementAndCarnetAnedOneParameter();
        createTemplateMessageSOS();

    }

    public void seedRoleUsersTable() {
        List<RolesUser> roles  = rolesRepository.findAll();
        if(roles.size() <= 0) {
          List<RolesUser> rolesUsers = new ArrayList<>(4);
          rolesUsers.add(new RolesUser(ERoles.ROLE_USER));
          rolesUsers.add(new RolesUser(ERoles.ROLE_ADMIN));
          rolesUsers.add(new RolesUser(ERoles.ROLE_MEDECIN));
          rolesUsers.add(new RolesUser(ERoles.ROLE_PRE_VERIFICATION_USER));
          rolesUsers.stream().map(role -> rolesRepository.save(role)).collect(Collectors.toList());
        } else {
            log.info("Roles seeding not required !!!");
        }
    }

    public void seedHospitalsTable() {
        List<Hospitals>  list = hospitalRepo.findAll();
        if(list.size() <= 0) {
            List<Hospitals> hospitals = new ArrayList<>(4);
            hospitals.add(new Hospitals("hopital general de douala","213116532232","3213213213","https://www.google.cm/maps/uv?pb=!1s0x10610db0febab2c9%3A0x355a698b55706320!3m1!7e115!4shttps%3A%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipN5VxJ72mHYMU9WydAj9uFiDO49aZa1w_QpSeOR%3Dw213-h160-k-no!5shopital%20general%20douala%20-%20Recherche%20Google!15sCgIgAQ&imagekey=!1e10!2sAF1QipN5VxJ72mHYMU9WydAj9uFiDO49aZa1w_QpSeOR&hl=fr&sa=X&ved=2ahUKEwiAueHb8YDxAhWXQUEAHTv2DlAQoiowEnoECD4QAw#","centre hospitalié public assermenté par l'ETAT"));
            hospitals.add(new Hospitals("hopital de nylon de yassa","25226596365","55156996588","https://www.google.cm/maps/uv?pb=!1s0x10610d5d9d80162f%3A0x78325db056beba4a!3m1!7e115!4shttps%3A%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipOc18F1fDqtjVP_L0W_7pnNsZt34n_cz4RGnXbH%3Dw284-h160-k-no!5shopital%20nylon%20-%20Recherche%20Google!15sCgIgAQ&imagekey=!1e10!2sAF1QipOc18F1fDqtjVP_L0W_7pnNsZt34n_cz4RGnXbH&hl=fr&sa=X&ved=2ahUKEwi73frb8oDxAhUGD8AKHcs7CUMQoiowDHoECDEQAw#","centre hospitalié public assermenté par l'ETAT"));
            hospitals.add(new Hospitals("hopital laquintinie de douala","255866254879368","25148956365","https://www.google.cm/maps/uv?pb=!1s0x1061128b0a565983%3A0x5c3817e7dad6dac2!3m1!7e115!4shttps%3A%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipPNCCDCDsGGfBXNnGlKTZC_eaLpoirWzN3u-o67%3Dw90-h160-k-no!5shopital%20laquintinie%20-%20Recherche%20Google!15sCgIgAQ&imagekey=!1e10!2sAF1QipPNCCDCDsGGfBXNnGlKTZC_eaLpoirWzN3u-o67&hl=fr&sa=X&ved=2ahUKEwj5qbrM8oDxAhXMX8AKHT4KBuEQoiowHnoECDkQAw#","centre hospitalié public assermenté par l'ETAT"));
          hospitals.stream().map(hospitals1 -> hospitalRepo.save(hospitals1)).collect(Collectors.toList());
        } else {
            log.info("Hospitals seeding not required!!!");
        }
    }

    public void seedStatusUsersTable() {
        List<StatusUsers>  list = statusUsersRepository.findAll();
        if( list.size() <=0) {
            List<StatusUsers> liststatus = new ArrayList<>(2);
            liststatus.add(new StatusUsers(EStatusUser.USER_ENABLED,"compte utilisateur actif"));
            liststatus.add(new StatusUsers(EStatusUser.USER_DISABLED,"compte utilisateur désactivé"));
            liststatus.stream().map(status -> statusUsersRepository.save(status)).collect(Collectors.toList());
        }else {
            log.info("Status seeding not required !!!");
        }
    }

    public void createPatientSetAbonnementAndCarnetAnedOneParameter() {
        List<Patient> patientList = patientRepo.findAll();
        if(patientList.size() <=0) {
            

            Patient patient1 = new Patient("franck","sfranckdabryn@gmail.com","Popin@2009");
            patient1.setTel1("+237693236244");
            patient1.setProviderName("local");
            Patient patientsave1 = patientService.savePatient(patient1);
            Abonnement abonnement1 = new Abonnement();
            abonnement1.setAmount(15000);
            abonnement1.setStartDate(LocalDateTime.now());
            abonnement1.setEndDate(LocalDateTime.now().plusDays(30));
            abonnement1.setEtat(true);
            abonnement1.setPatient(patientsave1);
            abonnement1.setHospitals(hospitalRepo.getOne(1L));
            abonnement1.setCreatedAt(LocalDate.now());
            abonnementRepo.save(abonnement1);
            Optional<StatusUsers> statusUsers1 = statusUsersRepository.findByName(EStatusUser.USER_ENABLED);
            patientsave1.setStatus(statusUsers1.get());
            patientRepo.save(patientsave1);
            Parametre param1 = new Parametre();
            param1.setCarnet(patientsave1.getCarnet());
            param1.setPoids(72.5);
            param1.setPouls(10.2);
            param1.setFrequenceCardiaque(125);
            param1.setEtat(true);
            param1.setCreatedAt(LocalDateTime.now());
            param1.setTemperature(47.5);
            param1.setPerimetreBranchial(125);
            Carnet carnet1 = carnetRepo.findByPatient(patientsave1);
            patientService.createParametre(param1,carnet1.getId());
        }else {
            log.info("patient seeder not required !!!");
        }
    }

    public void createOneDoctorsToHospital() {
        List<Doctors> doctors = doctorsRepo.findAll();
         if(doctors.size() <=0) {
             Doctors doctor = new Doctors("warren95","moudjiekemenifabrice@yahoo.fr","Fabrice@123");
             doctor.setTel1("+237696210719");
             doctor.setProviderName("local");
             Set<RolesUser> rolesUsers = new HashSet<>();
             rolesUsers.add(rolesRepository.findByName(ERoles.ROLE_USER).get());
             rolesUsers.add(rolesRepository.findByName(ERoles.ROLE_MEDECIN).get());
             doctor.setRoles(rolesUsers);
             Hospitals hospitals = hospitalRepo.getOne(1L);
             Doctors doctorsave = doctorService.saveDoctor(doctor);
             Optional<StatusUsers> statusUsers = statusUsersRepository.findByName(EStatusUser.USER_ENABLED);
              doctorsave.setStatus(statusUsers.get());
              doctorsave.setHospitals(hospitals);
             doctorsave.setSosReceived(true);
              doctorsRepo.save(doctorsave);
         }else {
             log.info("doctor seeder is not required !!!");
         }
    }

    public void createTemplateMessageSOS() {
        List<TemplateSos> list = templateSosRepo.findAll();
        if(list.size() <= 0) {
            TemplateSos templateSos = new TemplateSos();
            templateSos.setEtat(true);
            templateSos.setMessage("Bonjour Mr/Mme,\n" +
                    "J'ai un malaise bien vouloir m'envoyer une ambulance, mes précédent paramêtres médicaux sont les suivant : \n");
            templateSos.setCreatedAt(LocalDate.now());
            templateSosRepo.save(templateSos);
        } else {
            log.info("template sos seeder is not required !!!");
        }
    }


}
