package com.demo.funchat.repository;

import com.demo.funchat.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByGroupIdOrderBySentAtAsc(Long groupId);
}
