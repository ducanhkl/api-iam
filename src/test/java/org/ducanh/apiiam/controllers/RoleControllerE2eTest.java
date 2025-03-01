package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.entities.Role;
import org.ducanh.apiiam.repositories.GroupRoleRepository;
import org.ducanh.apiiam.repositories.PermissionRepository;
import org.ducanh.apiiam.repositories.RolePermissionRepository;
import org.ducanh.apiiam.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
public class RoleControllerE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoSpyBean
    private GroupRoleRepository groupRoleRepository;
    @MockitoSpyBean
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        Mockito.reset(groupRoleRepository);
        Mockito.reset(rolePermissionRepository);
        roleRepository.deleteAll();
    }

    @Test
    void shouldCreateRoleSuccessfully() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "roleId": "admin-role",
                    "roleName": "Administrator",
                    "description": "Administrator role with full access"
                }
                """)
                .when()
                .post("/role/namespace-id/{namespaceId}", "test-namespace")
                .then()
                .statusCode(201)
                .body("roleId", equalTo("admin-role"))
                .body("roleName", equalTo("Administrator"))
                .body("description", equalTo("Administrator role with full access"))
                .body("namespaceId", equalTo("test-namespace"))
                .body("createdAt", notNullValue());
    }

    @Test
    void shouldFailWhenCreatingDuplicateRole() {
        // Create initial role
        Role existingRole = Role.builder()
                .roleId("admin-role")
                .roleName("Administrator")
                .description("Administrator role")
                .namespaceId("test-namespace")
                .build();
        roleRepository.save(existingRole);

        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "roleId": "admin-role",
                    "roleName": "Administrator",
                    "description": "Administrator role"
                }
                """)
                .when()
                .post("/role/namespace-id/{namespaceId}", "test-namespace")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("ROLE_015_400"));
    }

    @Test
    void shouldGetRoleSuccessfully() {
        // Create a role first
        Role role = Role.builder()
                .roleId("test-role")
                .roleName("Test Role")
                .description("Test Description")
                .namespaceId("test-namespace")
                .build();
        roleRepository.save(role);

        given()
                .when()
                .get("/role/namespace-id/{namespaceId}/role-id/{roleId}", "test-namespace", "test-role")
                .then()
                .statusCode(200)
                .body("roleId", equalTo("test-role"))
                .body("roleName", equalTo("Test Role"))
                .body("description", equalTo("Test Description"));
    }

    @Test
    void shouldFailWhenGettingNonExistentRole() {
        given()
                .when()
                .get("/role/namespace-id/{namespaceId}/role-id/{roleId}", "test-namespace", "non-existent")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("ROLE_011_400"));
    }

    @Test
    void shouldUpdateRoleSuccessfully() {
        // Create initial role
        Role role = Role.builder()
                .roleId("test-role")
                .roleName("Original Name")
                .description("Original Description")
                .namespaceId("test-namespace")
                .build();
        roleRepository.save(role);

        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "roleName": "Updated Name",
                    "description": "Updated Description",
                    "namespaceId": "test-namespace"
                }
                """)
                .when()
                .put("/role/namespace-id/{namespaceId}/role-id/{roleId}", "test-namespace", "test-role")
                .then()
                .statusCode(200)
                .body("roleName", equalTo("Updated Name"))
                .body("description", equalTo("Updated Description"));

        Role updatedRole = roleRepository.findById("test-role").orElseThrow();
        assertEquals("Updated Name", updatedRole.getRoleName());
        assertEquals("Updated Description", updatedRole.getDescription());
    }

    @Test
    void shouldDeleteRoleSuccessfully() {
        // Create a role first
        Role role = Role.builder()
                .roleId("test-role")
                .roleName("Test Role")
                .description("Test Description")
                .namespaceId("test-namespace")
                .build();
        roleRepository.save(role);

        given()
                .when()
                .delete("/role/namespace-id/{namespaceId}/role-id/{roleId}", "test-namespace", "test-role")
                .then()
                .statusCode(204);
        verify(groupRoleRepository, times(1))
                .deleteAllByRoleIdAndNamespaceId(any(), any());
        verify(rolePermissionRepository, times(1))
                .deleteAllByRoleIdAndNamespaceId(any(), any());
        assertFalse(roleRepository.existsById("test-role"));
    }

    @Test
    void shouldListRolesWithPagination() {
        // Create multiple roles
        List<Role> roles = List.of(
                Role.builder()
                        .roleId("role-1")
                        .roleName("Admin Role")
                        .description("Admin Description")
                        .namespaceId("test-namespace")
                        .build(),
                Role.builder()
                        .roleId("role-2")
                        .roleName("User Role")
                        .description("User Description")
                        .namespaceId("test-namespace")
                        .build()
        );
        roleRepository.saveAll(roles);

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/role/namespace-id/{namespaceId}/index", "test-namespace")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }

    @Test
    void shouldFilterRolesByName() {
        // Create multiple roles
        List<Role> roles = List.of(
                Role.builder()
                        .roleId("role-1")
                        .roleName("Admin Role")
                        .description("Admin Description")
                        .namespaceId("test-namespace")
                        .build(),
                Role.builder()
                        .roleId("role-2")
                        .roleName("User Role")
                        .description("User Description")
                        .namespaceId("test-namespace")
                        .build()
        );
        roleRepository.saveAll(roles);

        given()
                .queryParam("roleName", "Admin")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/role/namespace-id/{namespaceId}/index", "test-namespace")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].roleName", containsString("Admin"));
    }

    @Test
    void shouldReturnEmptyListForNonExistentNamespace() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/role/namespace-id/{namespaceId}/index", "non-existent-namespace")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void shouldFailWhenCreatingRoleWithInvalidData() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "roleId": "",
                    "roleName": "",
                    "description": "Invalid role data"
                }
                """)
                .when()
                .post("/role/namespace-id/{namespaceId}", "test-namespace")
                .then()
                .statusCode(400);
    }
}