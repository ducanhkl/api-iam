package org.ducanh.apiiam.controllers;


import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.entities.Group;
import org.ducanh.apiiam.entities.GroupRole;
import org.ducanh.apiiam.entities.Role;
import org.ducanh.apiiam.repositories.GroupRepository;
import org.ducanh.apiiam.repositories.GroupRoleRepository;
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
class GroupRoleControllerE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GroupRoleRepository groupRoleRepository;

    private static final String NAMESPACE_ID = "test-namespace";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        groupRoleRepository.deleteAll();
        groupRepository.deleteAll();
        roleRepository.deleteAll();
    }

    private Group createTestGroup(String groupId, String groupName) {
        Group group = Group.builder()
                .groupId(groupId)
                .groupName(groupName)
                .description("Test Group Description")
                .namespaceId(NAMESPACE_ID)
                .build();
        return groupRepository.save(group);
    }

    private Role createTestRole(String roleId, String roleName) {
        Role role = Role.builder()
                .roleId(roleId)
                .roleName(roleName)
                .description("Test Role Description")
                .namespaceId(NAMESPACE_ID)
                .build();
        return roleRepository.save(role);
    }

    @Test
    void whenAssignRolesToGroup_thenSuccess() {
        // Given
        Group group = createTestGroup("test-group", "Test Group");
        Role role1 = createTestRole("role-1", "Role 1");
        Role role2 = createTestRole("role-2", "Role 2");

        // When
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "roleIds": ["role-1", "role-2"]
                }
                """)
                .when()
                .post("/group-role/{namespaceId}/group-id/{groupId}/roles", NAMESPACE_ID, group.getGroupId())
                .then()
                .statusCode(200);

        // Then
        List<GroupRole> assignments = groupRoleRepository.findAllByNamespaceIdAndGroupId(NAMESPACE_ID, group.getGroupId());
        assertEquals(2, assignments.size());
        assertTrue(assignments.stream().anyMatch(gr -> gr.getRoleId().equals(role1.getRoleId())));
        assertTrue(assignments.stream().anyMatch(gr -> gr.getRoleId().equals(role2.getRoleId())));
    }

    @Test
    void whenAssignRolesToNonExistentGroup_thenFail() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "roleIds": ["role-1"]
                }
                """)
                .when()
                .post("/group-role/{namespaceId}/group-id/{groupId}/roles", NAMESPACE_ID, "non-existent-group")
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("GROUP_009_400"))
                .body("shortDescriptions", equalTo("Group not found"));
    }

    @Test
    void whenAssignNonExistentRoles_thenFail() {
        // Given
        Group group = createTestGroup("test-group", "Test Group");

        // When/Then
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "roleIds": ["non-existent-role"]
                }
                """)
                .when()
                .post("/group-role/{namespaceId}/group-id/{groupId}/roles", NAMESPACE_ID, group.getGroupId())
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("ROLE_011_400"))
                .body("shortDescriptions", equalTo("Role not found"));
    }

    @Test
    void whenRemoveRolesFromGroup_thenSuccess() {
        // Given
        Group group = createTestGroup("test-group", "Test Group");
        Role role = createTestRole("test-role", "Test Role");

        GroupRole groupRole = GroupRole.builder()
                .groupId(group.getGroupId())
                .roleId(role.getRoleId())
                .namespaceId(NAMESPACE_ID)
                .build();
        groupRoleRepository.save(groupRole);

        // When
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "roleIds": ["test-role"]
                }
                """)
                .when()
                .delete("/group-role/{namespaceId}/group-id/{groupId}/roles", NAMESPACE_ID, group.getGroupId())
                .then()
                .statusCode(200);

        // Then
        List<GroupRole> remainingAssignments = groupRoleRepository.findAllByNamespaceIdAndGroupId(NAMESPACE_ID, group.getGroupId());
        assertTrue(remainingAssignments.isEmpty());
    }

    @Test
    void whenRemoveRolesFromNonExistentGroup_thenFail() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "roleIds": ["role-1"]
                }
                """)
                .when()
                .delete("/group-role/{namespaceId}/group-id/{groupId}/roles", NAMESPACE_ID, "non-existent-group")
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("GROUP_009_400"))
                .body("shortDescriptions", equalTo("Group not found"));
    }

    @Test
    void whenGetGroupRoles_thenSuccess() {
        // Given
        Group group = createTestGroup("test-group", "Test Group");
        Role role = createTestRole("test-role", "Test Role");

        GroupRole groupRole = GroupRole.builder()
                .groupId(group.getGroupId())
                .roleId(role.getRoleId())
                .namespaceId(NAMESPACE_ID)
                .build();
        groupRoleRepository.save(groupRole);

        // When/Then
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/group-role/{namespaceId}/group-id/{groupId}/roles", NAMESPACE_ID, group.getGroupId())
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].roleId", equalTo(role.getRoleId()))
                .body("[0].roleName", equalTo(role.getRoleName()))
                .body("[0].description", equalTo(role.getDescription()));
    }

    @Test
    void whenGetGroupRolesWithFilter_thenSuccess() {
        // Given
        Group group = createTestGroup("test-group", "Test Group");
        Role role1 = createTestRole("role-1", "Admin Role");
        Role role2 = createTestRole("role-2", "User Role");

        groupRoleRepository.saveAll(List.of(
                GroupRole.builder()
                        .groupId(group.getGroupId())
                        .roleId(role1.getRoleId())
                        .namespaceId(NAMESPACE_ID)
                        .build(),
                GroupRole.builder()
                        .groupId(group.getGroupId())
                        .roleId(role2.getRoleId())
                        .namespaceId(NAMESPACE_ID)
                        .build()
        ));

        // When/Then
        given()
                .queryParam("roleName", "Admin")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/group-role/{namespaceId}/group-id/{groupId}/roles", NAMESPACE_ID, group.getGroupId())
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].roleId", equalTo(role1.getRoleId()))
                .body("[0].roleName", equalTo(role1.getRoleName()));
    }

    @Test
    void whenGetRoleGroups_thenSuccess() {
        // Given
        Role role = createTestRole("test-role", "Test Role");
        Group group = createTestGroup("test-group", "Test Group");

        GroupRole groupRole = GroupRole.builder()
                .groupId(group.getGroupId())
                .roleId(role.getRoleId())
                .namespaceId(NAMESPACE_ID)
                .build();
        groupRoleRepository.save(groupRole);

        // When/Then
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/group-role/{namespaceId}/role-id/{roleId}/groups", NAMESPACE_ID, role.getRoleId())
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].groupId", equalTo(group.getGroupId()))
                .body("[0].groupName", equalTo(group.getGroupName()));
    }

    @Test
    void whenGetRoleGroupsForNonExistentRole_thenFail() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/group-role/{namespaceId}/role-id/{roleId}/groups", NAMESPACE_ID, "non-existent-role")
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("ROLE_011_400"))
                .body("shortDescriptions", equalTo("Role not found"));
    }

    @Test
    void whenGetRoleGroupsWithFilter_thenSuccess() {
        // Given
        Role role = createTestRole("test-role", "Test Role");
        Group group1 = createTestGroup("group-1", "Admin Group");
        Group group2 = createTestGroup("group-2", "User Group");

        groupRoleRepository.saveAll(List.of(
                GroupRole.builder()
                        .groupId(group1.getGroupId())
                        .roleId(role.getRoleId())
                        .namespaceId(NAMESPACE_ID)
                        .build(),
                GroupRole.builder()
                        .groupId(group2.getGroupId())
                        .roleId(role.getRoleId())
                        .namespaceId(NAMESPACE_ID)
                        .build()
        ));

        // When/Then
        given()
                .queryParam("groupName", "Admin")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/group-role/{namespaceId}/role-id/{roleId}/groups", NAMESPACE_ID, role.getRoleId())
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].groupId", equalTo(group1.getGroupId()))
                .body("[0].groupName", equalTo(group1.getGroupName()));
    }
}