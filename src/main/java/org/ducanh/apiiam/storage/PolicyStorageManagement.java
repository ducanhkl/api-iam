package org.ducanh.apiiam.storage;

import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.entities.GroupRoleIdOnly;
import org.ducanh.apiiam.entities.Namespace;
import org.ducanh.apiiam.entities.RolePermissionIdOnly;
import org.ducanh.apiiam.repositories.GroupRoleRepository;
import org.ducanh.apiiam.repositories.NamespaceRepository;
import org.ducanh.apiiam.repositories.RolePermissionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PolicyStorageManagement {

    private final Map<String, PolicyStorage> policies = new HashMap<>();
    private final GroupRoleRepository groupRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final NamespaceRepository namespaceRepository;

    public PolicyStorageManagement(
            GroupRoleRepository groupRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            NamespaceRepository namespaceRepository
    ) {
        this.groupRoleRepository = groupRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.namespaceRepository = namespaceRepository;
        initiatingPolicy();
    }

    private void initiatingPolicy() {
        List<Namespace> namespaces = namespaceRepository.findAll();
        namespaces.forEach(namespace -> {
            String namespaceId = namespace.getNamespaceId();
            log.info("Initiating for namespaceId: {}", namespaceId);
            initiatingPolicy(namespaceId);
        });
        log.info("Initiating policy for all namespaces, number of namespaces: {}", namespaces.size());
    }

    private synchronized void initiatingPolicy(String namespaceId) {
        List<GroupRoleIdOnly> groupRoleIdList = groupRoleRepository.findAllByNamespaceId(namespaceId);
        List<RolePermissionIdOnly> rolePermissionIdOnlyList = rolePermissionRepository.findAllByNamespaceId(namespaceId);
        policies.put(namespaceId, new PolicyStorage(namespaceId, groupRoleIdList, rolePermissionIdOnlyList));
    }

    public void reloadPolicy(String namespaceId) {
        initiatingPolicy(namespaceId);
    }

    public PolicyStorage getPolicyStorage(String namespaceId) {
        return policies.get(namespaceId);
    }

}