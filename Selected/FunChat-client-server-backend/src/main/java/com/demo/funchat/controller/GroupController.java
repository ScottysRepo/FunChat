package com.demo.funchat.controller;

import com.demo.funchat.entity.GroupEntity;
import com.demo.funchat.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Get group list of current user
     */
    @GetMapping("/list")
    public ResponseEntity<List<GroupEntity>> getGroups(@RequestParam Long userId) {
        List<GroupEntity> groups = groupService.getGroupsForUser(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * Create new group
     */
    @PostMapping("/create")
    public ResponseEntity<GroupEntity> createGroup(@RequestParam String groupName, @RequestParam Set<Long> memberIds) {
        GroupEntity group = groupService.createGroup(groupName, memberIds);
        return ResponseEntity.ok(group);
    }
}

