package org.ducanh.apiiam.storage;

import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.entities.GroupRoleIdOnly;
import org.ducanh.apiiam.entities.Namespace;
import org.ducanh.apiiam.entities.RolePermissionIdOnly;
import org.ducanh.apiiam.repositories.GroupRoleRepository;
import org.ducanh.apiiam.repositories.NamespaceRepository;
import org.ducanh.apiiam.repositories.RolePermissionRepository;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class PolicyStorageManagement {

    private final Map<String, PolicyStorage> policies = new HashMap<>();
    private final GroupRoleRepository groupRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final NamespaceRepository namespaceRepository;
    private final ThreadPoolTaskExecutor commonThreadPool;

    public PolicyStorageManagement(
            GroupRoleRepository groupRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            NamespaceRepository namespaceRepository,
            ThreadPoolTaskExecutor threadPoolTaskExecutor
    ) {
        this.groupRoleRepository = groupRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.namespaceRepository = namespaceRepository;
        this.commonThreadPool = threadPoolTaskExecutor;
        initiatingPolicy();
    }

    public void initiatingPolicy() {
        List<Namespace> namespaces = namespaceRepository.findAll();
        List<CompletableFuture<Void>> futures = namespaces.stream()
                .map((namespace) -> CompletableFuture.runAsync(() -> initiatingPolicy(namespace), commonThreadPool))
                .toList();
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("Finish init policy for all namespaces, number of namespaces: {}", namespaces.size());
    }

    private void initiatingPolicy(Namespace namespace) {
        String namespaceIdIntern = namespace.getNamespaceId().intern();
        log.info("Initiating policy for namespace: {}", namespaceIdIntern);
        synchronized (namespaceIdIntern) {
            List<GroupRoleIdOnly> groupRoleIdList = groupRoleRepository.findAllByNamespaceId(namespaceIdIntern);
            List<RolePermissionIdOnly> rolePermissionIdOnlyList = rolePermissionRepository.findAllByNamespaceId(namespaceIdIntern);
            policies.put(namespaceIdIntern, new PolicyStorage(namespace, groupRoleIdList, rolePermissionIdOnlyList));
            log.info("Succeed rebuilding policy for namespace: {}", namespace);
        }
    }

    public void reloadPolicy(String namespaceId) {
        Namespace namespace = namespaceRepository.findByNamespaceId(namespaceId);
        initiatingPolicy(namespace);
    }

    public PolicyStorage getPolicyStorage(String namespaceId) {
        return policies.get(namespaceId);
    }

}