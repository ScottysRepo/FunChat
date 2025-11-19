package com.demo.funchat.service;

import com.demo.funchat.entity.GroupEntity;
import com.demo.funchat.entity.UserEntity;
import com.demo.funchat.repository.GroupRepository;
import com.demo.funchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public List<GroupEntity> getGroupsForUser(Long userId) {
        return groupRepository.findByMembers_Id(userId);
    }

    public GroupEntity createGroup(String groupName, Set<Long> memberIds) {
        GroupEntity group = new GroupEntity();
        group.setGroupName(groupName);
        Set<UserEntity> members = new HashSet<>(userRepository.findAllById(memberIds));
        group.setMembers(members);
        return groupRepository.save(group);
    }
}

