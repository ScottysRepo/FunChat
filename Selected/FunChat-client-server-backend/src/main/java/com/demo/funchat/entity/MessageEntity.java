package com.demo.funchat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime sentAt;

    @ElementCollection
    @CollectionTable(
            name = "message_emotes",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @MapKeyColumn(name = "emote_id")
    @Column(name = "count")
    private Map<Integer,Integer> emotes = new HashMap<>();
}
