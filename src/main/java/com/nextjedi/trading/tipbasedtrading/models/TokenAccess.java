package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class TokenAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    private String userId;

    private String publicToken;
    private String accessToken;
//todo: add date for token and get only the latest token
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
