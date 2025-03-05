package org.ducanh.apiiam.config;

import org.ducanh.apiiam.repositories.GroupRoleRepository;
import org.ducanh.apiiam.repositories.NamespaceRepository;
import org.ducanh.apiiam.repositories.RolePermissionRepository;
import org.ducanh.apiiam.storage.PolicyStorageManagement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicyStorageManagementConfig {

    @Bean
    public PolicyStorageManagement policyStorageManagement(GroupRoleRepository groupRoleRepository,
                                                           RolePermissionRepository rolePermissionRepository,
                                                           NamespaceRepository namespaceRepository) {
        return new PolicyStorageManagement(groupRoleRepository, rolePermissionRepository, namespaceRepository);
    }
}
