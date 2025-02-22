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
    @Column(name = "policy_id", unique = true)
    private Integer policyId;

    @Column(name = "policy_name", length = 100)
    private String policyName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "namespace_id")
    private Integer namespaceId;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "min_strength")
    private Integer minStrength;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;
}
