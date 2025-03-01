package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.entities.Group;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.entities.UserGroup;
import org.ducanh.apiiam.entities.UserStatus;
import org.ducanh.apiiam.repositories.GroupRepository;
import org.ducanh.apiiam.repositories.UserGroupRepository;
import org.ducanh.apiiam.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
public class UserGroupControllerE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        userGroupRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createTestUser() {
        User user = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .namespaceId("test-namespace")
                .isVerified(true)
                .deleted(false)
                .status(UserStatus.ACTIVE)
                .mfaEnabled(false)
                .accountLocked(false)
                .build();
        return userRepository.save(user);
    }

    private Group createTestGroup(String groupId, String groupName) {
        Group group = Group.builder()
                .groupId(groupId)
                .groupName(groupName)
                .description("Test group description")
                .namespaceId("test-namespace")
                .build();
        return groupRepository.save(group);
    }

    @Test
    public void shouldAssignGroupsToUserSuccessfully() {
        User user = createTestUser();
        Group group1 = createTestGroup("group-1", "Group 1");
        Group group2 = createTestGroup("group-2", "Group 2");

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "groupIds": ["%s", "%s"]
                }
                """, group1.getGroupId(), group2.getGroupId()))
                .when()
                .post("/user-group/user-id/{userId}/groups", user.getUserId())
                .then()
                .statusCode(200);

        List<UserGroup> assignments = userGroupRepository.findAllByUserId(user.getUserId());
        assertEquals(2, assignments.size());
    }

    @Test
    void shouldFailWhenAssigningGroupsToNonExistentUser() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "groupIds": ["group-1"]
                }
                """)
                .when()
                .post("/user-group/user-id/{userId}/groups", 999999L)
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("USER_016_400"));
    }

    @Test
    void shouldFailWhenAssigningNonExistentGroups() {
        User user = createTestUser();

        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "groupIds": ["non-existent-group"]
                }
                """)
                .when()
                .post("/user-group/user-id/{userId}/groups", user.getUserId())
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("GROUP_009_400"))
                .body("shortDescriptions", equalTo("Group not found"));
    }

    @Test
    void shouldGetAssignedUserGroupsSuccessfully() {
        User user = createTestUser();
        Group group = createTestGroup("test-group", "Test Group");

        UserGroup userGroup = UserGroup.builder()
                .userId(user.getUserId())
                .groupId(group.getGroupId())
                .namespaceId("test-namespace")
                .build();
        userGroupRepository.save(userGroup);

        given()
                .queryParam("assignedOnly", true)
                .when()
                .get("/user-group/user-id/{userId}/groups", user.getUserId())
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].groupId", equalTo(group.getGroupId()))
                .body("[0].groupName", equalTo(group.getGroupName()))
                .body("[0].assigned", equalTo(true));
    }

    @Test
    void shouldGetAllGroupsWithAssignmentStatus() {
        User user = createTestUser();
        Group assignedGroup = createTestGroup("group-1", "Assigned Group");
        Group unassignedGroup = createTestGroup("group-2", "Unassigned Group");

        UserGroup userGroup = UserGroup.builder()
                .userId(user.getUserId())
                .groupId(assignedGroup.getGroupId())
                .namespaceId("test-namespace")
                .build();
        userGroupRepository.save(userGroup);

        given()
                .queryParam("assignedOnly", false)
                .when()
                .get("/user-group/user-id/{userId}/groups", user.getUserId())
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("findAll { it.assigned == true }.size()", equalTo(1))
                .body("findAll { it.assigned == false }.size()", equalTo(1));
    }

    @Test
    void shouldVerifyUserGroupsSuccessfully() {
        User user = createTestUser();
        Group group = createTestGroup("test-group", "Test Group");

        UserGroup userGroup = UserGroup.builder()
                .userId(user.getUserId())
                .groupId(group.getGroupId())
                .namespaceId("test-namespace")
                .build();
        userGroupRepository.save(userGroup);

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "groupIds": ["%s"]
                }
                """, group.getGroupId()))
                .when()
                .post("/user-group/user-id/{userId}/groups/verify", user.getUserId())
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].groupId", equalTo(group.getGroupId()))
                .body("[0].assignedAt", notNullValue());
    }

    @Test
    void shouldReturnEmptyListWhenVerifyingNonAssignedGroups() {
        User user = createTestUser();
        Group group = createTestGroup("test-group", "Test Group");

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "groupIds": ["%s"]
                }
                """, group.getGroupId()))
                .when()
                .post("/user-group/user-id/{userId}/groups/verify", user.getUserId())
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void shouldRemoveUserFromGroupsSuccessfully() {
        User user = createTestUser();
        Group group = createTestGroup("test-group", "Test Group");

        UserGroup userGroup = UserGroup.builder()
                .userId(user.getUserId())
                .groupId(group.getGroupId())
                .namespaceId("test-namespace")
                .build();
        userGroupRepository.save(userGroup);

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "groupIds": ["%s"]
                }
                """, group.getGroupId()))
                .when()
                .delete("/user-group/user-id/{userId}/groups/", user.getUserId())
                .then()
                .statusCode(200);

        List<UserGroup> remainingAssignments = userGroupRepository.findAllByUserId(user.getUserId());
        assertTrue(remainingAssignments.isEmpty());
    }

    @Test
    void shouldFailWhenRemovingGroupsFromNonExistentUser() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "groupIds": ["test-group"]
                }
                """)
                .when()
                .delete("/user-group/user-id/{userId}/groups/", 999999L)
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("USER_016_400"));
    }

    @Test
    void shouldHandleValidationErrorForEmptyGroupIds() {
        User user = createTestUser();

        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "groupIds": []
                }
                """)
                .when()
                .post("/user-group/user-id/{userId}/groups", user.getUserId())
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("VALIDATION_ERROR_002_400"))
                .body("shortDescriptions", equalTo("Validation error"));
    }
}