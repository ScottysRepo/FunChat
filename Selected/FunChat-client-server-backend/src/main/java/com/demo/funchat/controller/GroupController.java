package com.demo.funchat.controller;

import com.demo.funchat.entity.GroupEntity;
import com.demo.funchat.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Create a new group chat.
     */
    @PostMapping("/create")
    public ResponseEntity<GroupDTO> createGroup(@RequestParam String groupName,
                                                @RequestParam List<Long> memberIds) {
        GroupEntity g = groupService.createGroup(groupName, memberIds);
        return ResponseEntity.ok(new GroupDTO(g.getId(), g.getGroupName()));
    }

    // groups that a user belongs to 
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupDTO>> listForUser(@PathVariable Long userId) {
        var groups = groupService.findGroupsForUser(userId).stream()
                .map(g -> new GroupDTO(g.getId(), g.getGroupName()))
                .toList();
        return ResponseEntity.ok(groups);
    }

    // group by id 
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getById(@PathVariable Long groupId) {
        var g = groupService.getById(groupId);
        return ResponseEntity.ok(new GroupDTO(g.getId(), g.getGroupName()));
    }

    public record GroupDTO(Long id, String groupName) {}
}
