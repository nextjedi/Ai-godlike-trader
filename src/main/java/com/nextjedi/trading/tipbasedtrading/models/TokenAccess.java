package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@ToString
public class TokenAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    private String userId;
    private String publicToken;
    private String accessToken;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
