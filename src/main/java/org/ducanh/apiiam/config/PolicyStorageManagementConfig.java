package org.ducanh.apiiam.config;

import org.ducanh.apiiam.Constants;
import org.ducanh.apiiam.repositories.GroupRoleRepository;
import org.ducanh.apiiam.repositories.NamespaceRepository;
import org.ducanh.apiiam.repositories.RolePermissionRepository;
import org.ducanh.apiiam.storage.PolicyStorageManagement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PolicyStorageManagementConfig {

    @Bean
    public PolicyStorageManagement policyStorageManagement(GroupRoleRepository groupRoleRepository,
                                                           RolePermissionRepository rolePermissionRepository,
                                                           NamespaceRepository namespaceRepository,
                                                           @Qualifier(value = Constants.THREAD_EXECUTOR)
                                                           ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        return new PolicyStorageManagement(groupRoleRepository, rolePermissionRepository, namespaceRepository, threadPoolTaskExecutor);
    }
}
