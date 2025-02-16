package org.ducanh.apiiam.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
        RSA256, ECDSA
    }

    public enum KeyUsage {
        SIGNATURE, ENCRYPTION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate the primary key
    @Column(name = "key_id", nullable = false, updatable = false)
    private Long keyPairId; // Primary Key

    @Column(name = "kid", nullable = false, unique = true, length = 100)
    private String kid; // Unique Key Identifier

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey; // Public Key Value

    @Column(name = "encrypted_private_key", nullable = false, columnDefinition = "TEXT")
    private String encryptedPrivateKey;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // Indicates if the key is active

    @Column(name = "algorithm", nullable = false, length = 100)
    private Algorithm algorithm; // Algorithm used for the public key (e.g., "RSA", "ECDSA")

    @Column(name = "key_usage", nullable = false, length = 50)
    private KeyUsage keyUsage; // Usage of the key (e.g., "SIGNATURE", "ENCRYPTION")

    @Column(name = "key_rotation_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime keyRotationDate;

    @Column(name = "key_status", nullable = false, length = 50)
    private String keyStatus;

    @Column(name = "key_version", nullable = false)
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
