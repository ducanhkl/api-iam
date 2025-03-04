package org.ducanh.apiiam.storage;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.entities.GroupRole;
import org.ducanh.apiiam.entities.RolePermission;

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

    public PolicyStorage(String namespaceId, List<GroupRole> groupRoles,
                         List<RolePermission> rolePermissions) {
        Map<String, List<String>> mapRoleToPermission = rolePermissions.stream()
                .collect(Collectors.groupingBy(RolePermission::getRoleId,
                        Collectors.mapping(RolePermission::getPermissionId, Collectors.toList())));
        mapGroupIdToSetPermissionId = groupRoles.stream().collect(Collectors
                .groupingBy(GroupRole::getGroupId,
                        Collectors.flatMapping(
                                gr -> mapRoleToPermission.get(gr.getRoleId()).stream(), Collectors.toSet())));
        this.namespaceId = namespaceId;
    }

    public Boolean checkAccess(String groupId, String permissionId) {
        return mapGroupIdToSetPermissionId.getOrDefault(groupId, Collections.emptySet())
                .contains(permissionId);
    }

}
