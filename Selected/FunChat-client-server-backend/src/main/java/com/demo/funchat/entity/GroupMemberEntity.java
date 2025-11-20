package com.demo.funchat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_members")
public class GroupMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public GroupMemberEntity() {}

    public GroupMemberEntity(GroupEntity group, Long userId) {
        this.group = group;
        this.userId = userId;
    }

    // Getters and setters
    public Long getId() { return id; }

    public GroupEntity getGroup() { return group; }
    public void setGroup(GroupEntity group) { this.group = group; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
