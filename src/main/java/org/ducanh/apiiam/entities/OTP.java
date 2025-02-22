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
@Table(name = "otps") // Optional: Assigns a table name
@NoArgsConstructor // Generates a no-arguments constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Provides a builder pattern for easier instantiation
@Setter
@Getter
public class OTP {

    public enum Type {
        LOGIN,
        RESET_PASSWORD,
        VERIFY
    }

    private enum Method {
        EMAIL,
        SMS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "otp_id_seq")
    @SequenceGenerator(
            name = "otp_id_seq",
            sequenceName = "otp_id_seq",
            allocationSize = 100
    )
    @Column(name = "otp_id") // Explicit column name
    private Long otpId;

    @Column(name = "user_id") // Explicit column name
    private Long userId;

    @Column(name = "namespace_id") // Explicit column name
    private String namespaceId;

    @Column(name = "code", length = 10) // Explicit column name
    private String code;

    @Column(name = "expired_at") // Explicit column name
    private OffsetDateTime expiredAt;

    @Column(name = "used") // Explicit column name
    private Boolean used;

    @Column(name = "type", length = 20) // Explicit column name
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "method", length = 10) // Explicit column name
    @Enumerated(EnumType.STRING)
    private Method method;

    @Column(name = "retries") // Explicit column name
    private int retries;

    @Column(name = "is_verified") // Explicit column name
    private boolean isVerified;

    @Column(name = "email") // Explicit column name
    private String email;

    @Column(name = "phone_number") // Explicit column name
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE") // Explicit column name
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE") // Explicit column name
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public static OTP createOtpForVerify(
            User user,
            String code,
            OffsetDateTime expiredAt
    ) {
        return OTP.builder().userId(user.getUserId())
                .code(code)
                .expiredAt(expiredAt)
                .used(false)
                .type(Type.VERIFY)
                .method(Method.SMS)
                .retries(0)
                .isVerified(false)
                .email(user.getEmail())
                .namespaceId(user.getNamespaceId())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    public void incrementRetries() {
        retries++;
    }

    public void makeVerified() {
        isVerified = true;
        used = true;
    }

//    @Column(length = 45)
//    private String ipAddress;
//
//    @Column(length = 255)
//    private String userAgent;
}