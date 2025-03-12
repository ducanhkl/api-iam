package org.ducanh.apiiam.storage;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.entities.GroupRoleIdOnly;
import org.ducanh.apiiam.entities.Namespace;
import org.ducanh.apiiam.entities.RolePermissionIdOnly;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class PolicyStorage {
    private final Map<String, Set<String>> mapGroupIdToSetPermissionId;

    @Getter
    private final String namespaceId;
    @Getter
    private final Long version;

    public PolicyStorage(Namespace namespace, List<GroupRoleIdOnly> groupRoles,
                         List<RolePermissionIdOnly> rolePermissions) {
        Map<String, List<String>> mapRoleToPermission = rolePermissions.stream()
                .collect(Collectors.groupingBy(RolePermissionIdOnly::roleId,
                        Collectors.mapping(RolePermissionIdOnly::permissionId, Collectors.toList())));
        mapGroupIdToSetPermissionId = groupRoles.stream().collect(Collectors
                .groupingBy(GroupRoleIdOnly::groupId,
                        Collectors.flatMapping(
                                gr -> mapRoleToPermission.get(gr.roleId()).stream(), Collectors.toSet())));
        this.namespaceId = namespace.getNamespaceId();
        this.version = namespace.getVersion();
    }

    public Boolean checkAccess(String groupId, String permissionId) {
        return mapGroupIdToSetPermissionId.getOrDefault(groupId, Collections.emptySet())
                .contains(permissionId);
    }

}
