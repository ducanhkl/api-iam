package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.entities.Permission;
import org.ducanh.apiiam.repositories.PermissionRepository;
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
class PermissionControllerE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PermissionRepository permissionRepository;

    private static final String NAMESPACE_ID = "test-namespace";
    private static final String BASE_PATH = "/permission/namespace-id/{namespaceId}/";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        permissionRepository.deleteAll();
    }

    private Permission createTestPermission(String permissionId, String permissionName) {
        Permission permission = Permission.builder()
                .permissionId(permissionId)
                .permissionName(permissionName)
                .description("Test Description")
                .namespaceId(NAMESPACE_ID)
                .build();
        return permissionRepository.save(permission);
    }

    @Test
    void whenCreatePermission_thenSuccess() {
        String requestBody = """
            {
                "permissionId": "test-permission",
                "permissionName": "Test Permission",
                "description": "Test Description"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH, NAMESPACE_ID)
                .then()
                .statusCode(201)
                .body("permissionId", equalTo("test-permission"))
                .body("permissionName", equalTo("Test Permission"))
                .body("description", equalTo("Test Description"))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());

        assertTrue(permissionRepository.existsById("test-permission"));
    }

    @Test
    void whenCreatePermissionWithInvalidData_thenFail() {
        String requestBody = """
            {
                "permissionId": "",
                "permissionName": "",
                "description": "Test Description"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH, NAMESPACE_ID)
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("UNKNOWN_002_400"))
                .body("shortDescriptions", equalTo("Validation error"));
    }

    @Test
    void whenGetPermission_thenSuccess() {
        Permission permission = createTestPermission("test-permission", "Test Permission");

        given()
                .when()
                .get(BASE_PATH + "permission-id/{permissionId}", NAMESPACE_ID, permission.getPermissionId())
                .then()
                .statusCode(200)
                .body("permissionId", equalTo(permission.getPermissionId()))
                .body("permissionName", equalTo(permission.getPermissionName()))
                .body("description", equalTo(permission.getDescription()));
    }

    @Test
    void whenGetNonExistentPermission_thenFail() {
        given()
                .when()
                .get(BASE_PATH + "permission-id/{permissionId}", NAMESPACE_ID, "non-existent")
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("PERMISSION_014_400"))
                .body("shortDescriptions", equalTo("Permission not exist"));
    }

    @Test
    void whenUpdatePermission_thenSuccess() {
        Permission permission = createTestPermission("test-permission", "Original Name");

        String requestBody = """
            {
                "permissionName": "Updated Name",
                "description": "Updated Description"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "permission-id/{permissionId}", NAMESPACE_ID, permission.getPermissionId())
                .then()
                .statusCode(200)
                .body("permissionName", equalTo("Updated Name"))
                .body("description", equalTo("Updated Description"));

        Permission updatedPermission = permissionRepository.findById(permission.getPermissionId()).orElseThrow();
        assertEquals("Updated Name", updatedPermission.getPermissionName());
        assertEquals("Updated Description", updatedPermission.getDescription());
    }

    @Test
    void whenUpdateNonExistentPermission_thenFail() {
        String requestBody = """
            {
                "permissionName": "Updated Name",
                "description": "Updated Description"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "permission-id/{permissionId}", NAMESPACE_ID, "non-existent")
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("PERMISSION_014_400"))
                .body("shortDescriptions", equalTo("Permission not exist"));
    }

    @Test
    void whenDeletePermission_thenSuccess() {
        Permission permission = createTestPermission("test-permission", "Test Permission");

        given()
                .when()
                .delete(BASE_PATH + "permission-id/{permissionId}", NAMESPACE_ID, permission.getPermissionId())
                .then()
                .statusCode(204);

        assertFalse(permissionRepository.existsById(permission.getPermissionId()));
    }

    @Test
    void whenDeleteNonExistentPermission_thenFail() {
        given()
                .when()
                .delete(BASE_PATH + "permission-id/{permissionId}", NAMESPACE_ID, "non-existent")
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("PERMISSION_014_400"))
                .body("shortDescriptions", equalTo("Permission not exist"));
    }

    @Test
    void whenIndexPermissions_thenSuccess() {
        List<Permission> permissions = List.of(
                createTestPermission("permission-1", "Test Permission 1"),
                createTestPermission("permission-2", "Test Permission 2")
        );

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get(BASE_PATH + "index", NAMESPACE_ID)
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("findAll { it.permissionName.startsWith('Test Permission') }", hasSize(2));
    }

    @Test
    void whenIndexPermissionsWithNameFilter_thenSuccess() {
        createTestPermission("permission-1", "Admin Permission");
        createTestPermission("permission-2", "User Permission");

        given()
                .queryParam("permissionName", "Admin Permission")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get(BASE_PATH + "index", NAMESPACE_ID)
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].permissionName", equalTo("Admin Permission"));
    }
}