package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quickbooks_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickBooksToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String realmId;

    @Column(nullable = false, length = 1024) // or use @Lob for TEXT
    private String accessToken;

    @Column(nullable = false, length = 1024)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // token expiration time

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
