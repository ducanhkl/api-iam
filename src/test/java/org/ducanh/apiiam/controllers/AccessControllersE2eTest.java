package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.repositories.*;
import org.ducanh.apiiam.storage.PolicyStorageManagement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
class AccessControllersE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private GroupRoleRepository groupRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PolicyStorageManagement policyStorageManagement;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    private static final String NAMESPACE_ID = "master";

    @BeforeEach
    public void beforeEach1() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        groupRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        groupRoleRepository.deleteAll();
        rolePermissionRepository.deleteAll();
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection().serverCommands().flushAll();
        policyStorageManagement.initiatingPolicy();
    }

    private void createGroup(String groupId) {
        String requestBody = String.format("""
                {
                    "groupId": "%s",
                    "groupName": "%s-name",
                    "description": "%s-descriptions"
                }
                """, groupId, groupId, groupId);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/group/namespace-id/" + NAMESPACE_ID)
                .then()
                .statusCode(201);
    }

    private void createRole(String roleId) {
        String requestBody = String.format("""
                {
                    "roleId": "%s",
                    "roleName": "%s-name",
                    "description": "%s-descriptions"
                }
                """, roleId, roleId, roleId);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/role/namespace-id/" + NAMESPACE_ID)
                .then()
                .statusCode(201);
    }

    private void createPermission(String permissionId) {
        String requestBody = String.format("""
                {
                    "permissionId": "%s",
                    "permissionName": "%s-name",
                    "description": "%s-descriptions"
                }
                """, permissionId, permissionId, permissionId);
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/permission/namespace-id/{namespaceId}", NAMESPACE_ID)
                .then()
                .statusCode(201);
    }

    private void assignRoleForGroup(String roleId, String groupId) {
        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "roleIds": ["%s"]
                }
                """, roleId))
                .when()
                .post("/group-role/{namespaceId}/group-id/{groupId}/roles", NAMESPACE_ID, groupId)
                .then()
                .statusCode(200);
    }

    private void assignPermissionForRole(String permissionId, String roleId) {
        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "permissionIds": ["%s"]
                }
                """, permissionId))
                .when()
                .post("/role-permission/{namespaceId}/role-id/{roleId}/permissions", NAMESPACE_ID, roleId)
                .then()
                .statusCode(200);
    }

    private void checkAccess(String groupId, String permissionId, Boolean assertAccess) {
        String checkAccessRequest = String.format("""
                {
                    "groupId": ["%s"],
                    "permissionId": "%s"
                }
                """, groupId, permissionId);

        given()
                .contentType(ContentType.JSON)
                .header("namespace-id", NAMESPACE_ID)
                .body(checkAccessRequest)
                .when()
                .post("/access/check-access")
                .then()
                .statusCode(200)
                .body("canAccess", equalTo(assertAccess));
    }

    @Test
    public void testBasicPermissionScenarios() {
        // Define simple, clear permission IDs
        String readPermission = "read";
        String writePermission = "write";
        String deletePermission = "delete";
        String adminPermission = "admin";

        // Define clear role IDs
        String adminRole = "admin_role";
        String editorRole = "editor_role";
        String viewerRole = "viewer_role";

        // Define clear group IDs
        String adminGroup = "admin_group";
        String editorGroup = "editor_group";
        String viewerGroup = "viewer_group";

        // 1. Create all entities
        // Create permissions
        createPermission(readPermission);
        createPermission(writePermission);
        createPermission(deletePermission);
        createPermission(adminPermission);

        // Create roles
        createRole(adminRole);
        createRole(editorRole);
        createRole(viewerRole);

        // Create groups
        createGroup(adminGroup);
        createGroup(editorGroup);
        createGroup(viewerGroup);

        // 2. Assign permissions to roles
        // Admin role gets all permissions
        assignPermissionForRole(readPermission, adminRole);
        assignPermissionForRole(writePermission, adminRole);
        assignPermissionForRole(deletePermission, adminRole);
        assignPermissionForRole(adminPermission, adminRole);

        // Editor role gets read and write permissions
        assignPermissionForRole(readPermission, editorRole);
        assignPermissionForRole(writePermission, editorRole);

        // Viewer role gets only read permission
        assignPermissionForRole(readPermission, viewerRole);

        // 3. Assign roles to groups
        assignRoleForGroup(adminRole, adminGroup);
        assignRoleForGroup(editorRole, editorGroup);
        assignRoleForGroup(viewerRole, viewerGroup);
        // 4. Test all permissions
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    // Test admin group permissions
                    checkAccess(adminGroup, readPermission, true);
                    checkAccess(adminGroup, writePermission, true);
                    checkAccess(adminGroup, deletePermission, true);
                    checkAccess(adminGroup, adminPermission, true);

                    // Test editor group permissions
                    checkAccess(editorGroup, readPermission, true);
                    checkAccess(editorGroup, writePermission, true);
                    checkAccess(editorGroup, deletePermission, false);
                    checkAccess(editorGroup, adminPermission, false);

                    // Test viewer group permissions
                    checkAccess(viewerGroup, readPermission, true);
                    checkAccess(viewerGroup, writePermission, false);
                    checkAccess(viewerGroup, deletePermission, false);
                    checkAccess(viewerGroup, adminPermission, false);
                });
    }



}
