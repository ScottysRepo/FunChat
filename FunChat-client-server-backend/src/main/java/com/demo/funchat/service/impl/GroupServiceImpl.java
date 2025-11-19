package com.demo.funchat.service.impl;

import com.demo.funchat.entity.GroupEntity;
import com.demo.funchat.entity.GroupMemberEntity;
import com.demo.funchat.repository.GroupRepository;
import com.demo.funchat.repository.GroupMemberRepository;
import com.demo.funchat.repository.UserRepository;
import com.demo.funchat.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupServiceImpl(GroupRepository groupRepository,
                            GroupMemberRepository groupMemberRepository,
                            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public GroupEntity createGroup(String groupName, List<Long> memberIds) {
        GroupEntity group = new GroupEntity();
        group.setGroupName(groupName);
        group = groupRepository.save(group);

for (Long uid : memberIds) {
    var userOpt = userRepository.findById(uid);
    if (userOpt.isEmpty()) {
        // just skip invalid ids, or log:
        System.out.println("Skipping non-existing user id " + uid);
        continue;
    }
    GroupMemberEntity member = new GroupMemberEntity();
    member.setGroup(group);
    member.setUserId(uid);
    groupMemberRepository.save(member);
}


        return group;
    }

    @Override
    public List<GroupEntity> findGroupsForUser(Long userId) {
        return groupRepository.findByMemberUserId(userId);
    }

    @Override
    public GroupEntity getById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupId));
    }
}
