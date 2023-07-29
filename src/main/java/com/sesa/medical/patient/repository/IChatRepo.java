package com.sesa.medical.patient.repository;

import com.sesa.medical.patient.entities.Chat;
import com.sesa.medical.patient.entities.EStatusMessage;
import com.sesa.medical.users.entities.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface IChatRepo extends JpaRepository<Chat, Long> {

    List<Chat> findByReceiverAndSender(Users users,Users sender);
    List<Chat> findByReceiverAndSenderAndStatusMessage(Users users, Users sender, EStatusMessage statusMessage);
    @Query(value = "SELECT DISTINCT by (sender_id), id,sender_id,receiver_id,message,status_message,message_type, createAt FROM  chat  where receiver_id=:x  ORDER BY sender_id,created_at DESC",nativeQuery = true)
    List<Object> getAllSender(@Param("x") Long id_user);



    int countChatByReceiverAndSenderAndStatusMessage(Users receiver,Users sender, EStatusMessage statusMessage);
}
