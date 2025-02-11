package org.ducanh.apiiam.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "password_policy")
public class PasswordPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id", nullable = false, updatable = false)
    private Integer policyId;

    @Column(name = "policy_name", nullable = false, unique = true, length = 100)
    private String policyName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "namespace_id", nullable = false)
    private Integer namespaceId;

    @Column(name = "max_age", nullable = false)
    private Integer maxAge;

    @Column(name = "min_strength", nullable = false)
    private Integer minStrength;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;
}
