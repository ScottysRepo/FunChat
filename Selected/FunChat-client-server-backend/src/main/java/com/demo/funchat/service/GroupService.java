package com.demo.funchat.service;

import com.demo.funchat.entity.GroupEntity;
import java.util.List;

public interface GroupService {
    GroupEntity createGroup(String groupName, List<Long> memberIds);
    List<GroupEntity> findGroupsForUser(Long userId);
    GroupEntity getById(Long groupId);
}
