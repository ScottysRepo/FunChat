package com.demo.funchat.repository;

import com.demo.funchat.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    List<GroupEntity> findByMembers_Id(Long userId);
}


