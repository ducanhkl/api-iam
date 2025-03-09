package org.ducanh.apiiam.controllers;


import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.entities.Group;
import org.ducanh.apiiam.entities.Namespace;
import org.ducanh.apiiam.repositories.GroupRepository;
import org.ducanh.apiiam.repositories.GroupRoleRepository;
import org.ducanh.apiiam.repositories.NamespaceRepository;
import org.ducanh.apiiam.repositories.UserGroupRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
public class GroupControllerE2eTest {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private NamespaceRepository namespaceRepository;

    @MockitoSpyBean
    private GroupRoleRepository groupRoleRepository;
    @MockitoSpyBean
    private UserGroupRepository userGroupRepository;

    @LocalServerPort
    private int port;

    private static final String NAMESPACE_ID = "master";

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        groupRepository.deleteAll();
    }

    @Test
    void whenCreateGroup_thenSuccess() {
        String requestBody = """
                {
                    "groupId": "test-group",
                    "groupName": "Test Group",
                    "description": "Test Description"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/group/namespace-id/" + NAMESPACE_ID)
                .then()
                .statusCode(201)
                .body("groupId", equalTo("test-group"))
                .body("groupName", equalTo("Test Group"))
                .body("description", equalTo("Test Description"));
    }

    @Test
    void whenCreateDuplicateGroup_thenFail() {
        // Create initial group
        Group group = Group.builder()
                .groupId("duplicate-group")
                .groupName("Duplicate Group")
                .description("Test Description")
                .namespaceId(NAMESPACE_ID)
                .build();
        groupRepository.save(group);

        String requestBody = """
                {
                    "groupId": "duplicate-group",
                    "groupName": "Duplicate Group",
                    "description": "Test Description"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/group/namespace-id/" + NAMESPACE_ID)
                .then()
                .statusCode(400)
                .body("errorCode", containsString("GROUP_010_400"));
    }

    @Test
    void whenGetExistingGroup_thenSuccess() {
        // Create a group first
        Group group = Group.builder()
                .groupId("get-group")
                .groupName("Get Group")
                .description("Test Description")
                .namespaceId(NAMESPACE_ID)
                .build();
        groupRepository.save(group);

        given()
                .when()
                .get("/group/namespace-id/" + NAMESPACE_ID + "/group-id/get-group")
                .then()
                .statusCode(200)
                .body("groupId", equalTo("get-group"))
                .body("groupName", equalTo("Get Group"))
                .body("description", equalTo("Test Description"));
    }

    @Test
    void whenGetNonExistentGroup_thenFail() {
        given()
                .when()
                .get("/group/namespace-id/" + NAMESPACE_ID + "/group-id/non-existent")
                .then()
                .statusCode(400); // Assuming your error handling returns 500 for this case
    }

    @Test
    void whenUpdateGroup_thenSuccess() {
        // Create initial group
        Group group = Group.builder()
                .groupId("update-group")
                .groupName("Original Name")
                .description("Original Description")
                .namespaceId(NAMESPACE_ID)
                .build();
        groupRepository.save(group);

        String requestBody = """
                {
                    "groupName": "Updated Name",
                    "description": "Updated Description"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/group/namespace-id/" + NAMESPACE_ID + "/group-id/update-group")
                .then()
                .statusCode(200)
                .body("groupName", equalTo("Updated Name"))
                .body("description", equalTo("Updated Description"));
    }

    @Test
    void whenDeleteGroup_thenSuccess() {
        resetAllNamespaces();
        // Create a group first
        Group group = Group.builder()
                .groupId("delete-group")
                .groupName("Delete Group")
                .description("Test Description")
                .namespaceId(NAMESPACE_ID)
                .build();
        groupRepository.save(group);
        given()
                .when()
                .delete("/group/namespace-id/" + NAMESPACE_ID + "/group-id/delete-group")
                .then()
                .statusCode(204);
        verify(groupRoleRepository, times(1))
                .deleteAllByGroupIdAndNamespaceId(any(), any());
        verify(userGroupRepository, times(1))
                .deleteAllByGroupIdAndNamespaceId(any(), any());
        Namespace namespace = namespaceRepository.findByNamespaceId(NAMESPACE_ID);
        Assertions.assertEquals(1, namespace.getVersion());
    }

    private void resetAllNamespaces() {
        var namespaces = namespaceRepository.findAll();
        namespaces.forEach((namespace) -> {
            namespace.setVersion(0L);
            namespaceRepository.save(namespace);
        });
    }

    @Test
    void whenIndexGroups_thenSuccess() {
        // Create multiple groups
        Group group1 = Group.builder()
                .groupId("group-1")
                .groupName("Test Group 1")
                .description("Description 1")
                .namespaceId(NAMESPACE_ID)
                .build();
        Group group2 = Group.builder()
                .groupId("group-2")
                .groupName("Test Group 2")
                .description("Description 2")
                .namespaceId(NAMESPACE_ID)
                .build();
        groupRepository.save(group1);
        groupRepository.save(group2);

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/group/namespace-id/" + NAMESPACE_ID + "/index")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("findAll { it.groupName.startsWith('Test Group') }", hasSize(2));
    }

    @Test
    void whenIndexGroupsWithFilter_thenSuccess() {
        // Create multiple groups
        Group group1 = Group.builder()
                .groupId("group-1")
                .groupName("Admin Group")
                .description("Description 1")
                .namespaceId(NAMESPACE_ID)
                .build();
        Group group2 = Group.builder()
                .groupId("group-2")
                .groupName("User Group")
                .description("Description 2")
                .namespaceId(NAMESPACE_ID)
                .build();
        groupRepository.save(group1);
        groupRepository.save(group2);

        given()
                .queryParam("groupName", "Admin")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/group/namespace-id/" + NAMESPACE_ID + "/index")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].groupName", equalTo("Admin Group"));
    }
}