package com.demo.funchat.repository;

import com.demo.funchat.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    @Query("SELECT g FROM GroupEntity g JOIN GroupMemberEntity m ON g.id = m.group.id WHERE m.userId = :userId")
    List<GroupEntity> findByMemberUserId(Long userId);
}
