package org.ducanh.apiiam.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "password_history")
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_id_seq")
    @SequenceGenerator(
            name = "history_id_seq",
            sequenceName = "history_id_seq",
            allocationSize = 100
    )
    private Integer historyId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "pwd_alg", length = 50)
    private String pwdAlg;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

//    @Column(name = "salt", nullable = false, length = 255)
//    private String salt;
}