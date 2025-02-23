package org.ducanh.apiiam.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;


@Entity
@Table(name = "key_pairs") // Table name in the database
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyPair {

    public enum Algorithm {
        RSA, ECDSA
    }

    public enum KeyUsage {
        SIGNATURE, ENCRYPTION
    }

    public enum KeyStatus {
        ACTIVE, DISABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate the primary key
    @Column(name = "key_id")
    private Long keyPairId; // Primary Key

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey; // Public Key Value

    @Column(name = "encrypted_private_key", columnDefinition = "TEXT")
    private String encryptedPrivateKey;

    @Column(name = "is_active")
    private Boolean isActive; // Indicates if the key is active

    @Column(name = "algorithm", length = 100)
    @Enumerated(EnumType.STRING)
    private Algorithm algorithm; // Algorithm used for the public key (e.g., "RSA", "ECDSA")

    @Column(name = "key_usage", length = 50)
    @Enumerated(EnumType.STRING)
    private KeyUsage keyUsage; // Usage of the key (e.g., "SIGNATURE", "ENCRYPTION")

    @Column(name = "key_rotation_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime keyRotationDate;

    @Column(name = "key_status", length = 50)
    @Enumerated(EnumType.STRING)
    private KeyStatus keyStatus;

    @Column(name = "key_version")
    private Integer keyVersion; // Version number of the key

    @Column(name = "expiry_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime expiryDate;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
