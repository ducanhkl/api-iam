package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.entities.Permission;
import org.ducanh.apiiam.entities.Role;
import org.ducanh.apiiam.entities.RolePermission;
import org.ducanh.apiiam.repositories.PermissionRepository;
import org.ducanh.apiiam.repositories.RolePermissionRepository;
import org.ducanh.apiiam.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
public class RolePermissionControllerE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        rolePermissionRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    private Role createTestRole(String roleId, String roleName) {
        Role role = Role.builder()
                .roleId(roleId)
                .roleName(roleName)
                .description("Test role description")
                .namespaceId("test-namespace")
                .build();
        return roleRepository.save(role);
    }

    private Permission createTestPermission(String permissionId, String permissionName) {
        Permission permission = Permission.builder()
                .permissionId(permissionId)
                .permissionName(permissionName)
                .description("Test permission description")
                .namespaceId("test-namespace")
                .build();
        return permissionRepository.save(permission);
    }

    @Test
    void shouldAssignPermissionsToRoleSuccessfully() {
        Role role = createTestRole("test-role", "Test Role");
        Permission permission1 = createTestPermission("permission-1", "Permission 1");
        Permission permission2 = createTestPermission("permission-2", "Permission 2");

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "permissionIds": ["%s", "%s"]
                }
                """, permission1.getPermissionId(), permission2.getPermissionId()))
                .when()
                .post("/role-permission/{namespaceId}/role-id/{roleId}/permissions", "test-namespace", role.getRoleId())
                .then()
                .statusCode(200);

        List<RolePermission> assignments = rolePermissionRepository
                .findAllByNamespaceIdAndRoleId("test-namespace", role.getRoleId());
        assertEquals(2, assignments.size());
    }

    @Test
    void shouldFailWhenAssigningToNonExistentRole() {
        Permission permission = createTestPermission("test-permission", "Test Permission");

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "permissionIds": ["%s"]
                }
                """, permission.getPermissionId()))
                .when()
                .post("/role-permission/{namespaceId}/role-id/{roleId}/permissions", "test-namespace", "non-existent-role")
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("ROLE_011_400"))
                .body("shortDescriptions", equalTo("Role not found"));
    }

    @Test
    void shouldRemovePermissionsFromRoleSuccessfully() {
        Role role = createTestRole("test-role", "Test Role");
        Permission permission = createTestPermission("test-permission", "Test Permission");

        RolePermission rolePermission = RolePermission.builder()
                .roleId(role.getRoleId())
                .permissionId(permission.getPermissionId())
                .namespaceId("test-namespace")
                .build();
        rolePermissionRepository.save(rolePermission);

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "permissionIds": ["%s"]
                }
                """, permission.getPermissionId()))
                .when()
                .delete("/role-permission/{namespaceId}/role-id/{roleId}/permissions", "test-namespace", role.getRoleId())
                .then()
                .statusCode(200);

        List<RolePermission> remainingAssignments = rolePermissionRepository
                .findAllByNamespaceIdAndRoleId("test-namespace", role.getRoleId());
        assertTrue(remainingAssignments.isEmpty());
    }

    @Test
    void shouldGetRolePermissionsSuccessfully() {
        Role role = createTestRole("test-role", "Test Role");
        Permission permission = createTestPermission("test-permission", "Test Permission");

        RolePermission rolePermission = RolePermission.builder()
                .roleId(role.getRoleId())
                .permissionId(permission.getPermissionId())
                .namespaceId("test-namespace")
                .build();
        rolePermissionRepository.save(rolePermission);

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/role-permission/{namespaceId}/role-id/{roleId}/permissions", "test-namespace", role.getRoleId())
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].permissionId", equalTo(permission.getPermissionId()))
                .body("[0].permissionName", equalTo(permission.getPermissionName()));
    }

    @Test
    void shouldGetRolePermissionsWithNameFilter() {
        Role role = createTestRole("test-role", "Test Role");
        Permission permission1 = createTestPermission("permission-1", "Admin Permission");
        Permission permission2 = createTestPermission("permission-2", "User Permission");

        List<RolePermission> rolePermissions = List.of(
                RolePermission.builder()
                        .roleId(role.getRoleId())
                        .permissionId(permission1.getPermissionId())
                        .namespaceId("test-namespace")
                        .build(),
                RolePermission.builder()
                        .roleId(role.getRoleId())
                        .permissionId(permission2.getPermissionId())
                        .namespaceId("test-namespace")
                        .build()
        );
        rolePermissionRepository.saveAll(rolePermissions);

        given()
                .queryParam("permissionName", "Admin")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/role-permission/{namespaceId}/role-id/{roleId}/permissions", "test-namespace", role.getRoleId())
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].permissionName", containsString("Admin"));
    }

    @Test
    void shouldGetPermissionRolesSuccessfully() {
        Role role = createTestRole("test-role", "Test Role");
        Permission permission = createTestPermission("test-permission", "Test Permission");

        RolePermission rolePermission = RolePermission.builder()
                .roleId(role.getRoleId())
                .permissionId(permission.getPermissionId())
                .namespaceId("test-namespace")
                .build();
        rolePermissionRepository.save(rolePermission);

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/role-permission/{namespaceId}/permission-id/{permissionId}/roles",
                        "test-namespace", permission.getPermissionId())
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].roleId", equalTo(role.getRoleId()))
                .body("[0].roleName", equalTo(role.getRoleName()));
    }

    @Test
    void shouldGetPermissionRolesWithNameFilter() {
        Permission permission = createTestPermission("test-permission", "Test Permission");
        Role role1 = createTestRole("role-1", "Admin Role");
        Role role2 = createTestRole("role-2", "User Role");

        List<RolePermission> rolePermissions = List.of(
                RolePermission.builder()
                        .roleId(role1.getRoleId())
                        .permissionId(permission.getPermissionId())
                        .namespaceId("test-namespace")
                        .build(),
                RolePermission.builder()
                        .roleId(role2.getRoleId())
                        .permissionId(permission.getPermissionId())
                        .namespaceId("test-namespace")
                        .build()
        );
        rolePermissionRepository.saveAll(rolePermissions);

        given()
                .queryParam("roleName", "Admin")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/role-permission/{namespaceId}/permission-id/{permissionId}/roles",
                        "test-namespace", permission.getPermissionId())
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].roleName", containsString("Admin"));
    }

    @Test
    void shouldFailWhenAssigningNonExistentPermissions() {
        Role role = createTestRole("test-role", "Test Role");

        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "permissionIds": ["non-existent-permission"]
                }
                """)
                .when()
                .post("/role-permission/{namespaceId}/role-id/{roleId}/permissions", "test-namespace", role.getRoleId())
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("PERMISSION_014_400"))
                .body("shortDescriptions", equalTo("Permission not exist"));
    }
}