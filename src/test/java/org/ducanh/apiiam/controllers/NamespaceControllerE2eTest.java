package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.entities.KeyPair;
import org.ducanh.apiiam.entities.Namespace;
import org.ducanh.apiiam.repositories.KeyPairRepository;
import org.ducanh.apiiam.repositories.NamespaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
public class NamespaceControllerE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private NamespaceRepository namespaceRepository;

    @Autowired
    private KeyPairRepository keyPairRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        namespaceRepository.deleteAll();
        keyPairRepository.deleteAll();
    }

    private KeyPair createTestKeyPair() {
        KeyPair keyPair = KeyPair.builder()
                .publicKey("test-public-key")
                .encryptedPrivateKey("test-private-key")
                .isActive(true)
                .algorithm(KeyPair.Algorithm.RSA)
                .keyUsage(KeyPair.KeyUsage.SIGNATURE)
                .keyStatus(KeyPair.KeyStatus.ACTIVE)
                .keyVersion(1)
                .expiryDate(OffsetDateTime.now().plusYears(1))
                .build();
        return keyPairRepository.save(keyPair);
    }

    @Test
    void whenCreateNamespace_thenSuccess() {
        // Given
        KeyPair keyPair = createTestKeyPair();

        // When
        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "namespaceId": "test-namespace",
                    "namespaceName": "Test Namespace",
                    "description": "Test Description",
                    "keyPairId": %d
                }
                """, keyPair.getKeyPairId()))
                .when()
                .post("/namespace")
                .then()
                .statusCode(201)
                .body("namespaceId", equalTo("test-namespace"))
                .body("namespaceName", equalTo("Test Namespace"))
                .body("description", equalTo("Test Description"))
                .body("keyPairId", equalTo(keyPair.getKeyPairId().intValue()))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test
    void whenCreateNamespaceWithDuplicateId_thenFail() {
        // Given
        KeyPair keyPair = createTestKeyPair();
        Namespace existingNamespace = Namespace.builder()
                .namespaceId("test-namespace")
                .namespaceName("Existing Namespace")
                .keyPairId(keyPair.getKeyPairId())
                .build();
        namespaceRepository.save(existingNamespace);

        // When/Then
        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "namespaceId": "test-namespace",
                    "namespaceName": "New Namespace",
                    "description": "Test Description",
                    "keyPairId": %d
                }
                """, keyPair.getKeyPairId()))
                .when()
                .post("/namespace")
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("NAMESPACE_012_400"))
                .body("longDescription", containsString("NamespaceId: test-namespace is duplicated"));
    }

    @Test
    void whenCreateNamespaceWithInvalidKeyPair_thenFail() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "namespaceId": "test-namespace",
                    "namespaceName": "Test Namespace",
                    "description": "Test Description",
                    "keyPairId": 999999
                }
                """)
                .when()
                .post("/namespace")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("KEYPAIR_013_400"));
    }

    @Test
    void whenGetNamespace_thenSuccess() {
        // Given
        KeyPair keyPair = createTestKeyPair();
        Namespace namespace = Namespace.builder()
                .namespaceId("test-namespace")
                .namespaceName("Test Namespace")
                .description("Test Description")
                .keyPairId(keyPair.getKeyPairId())
                .build();
        namespaceRepository.save(namespace);

        // When/Then
        given()
                .when()
                .get("/namespace/test-namespace")
                .then()
                .statusCode(200)
                .body("namespaceId", equalTo("test-namespace"))
                .body("namespaceName", equalTo("Test Namespace"))
                .body("description", equalTo("Test Description"))
                .body("keyPairId", equalTo(keyPair.getKeyPairId().intValue()));
    }

    @Test
    void whenGetNonExistentNamespace_thenFail() {
        given()
                .when()
                .get("/namespace/non-existent")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("NAMESPACE_003_400"));
    }

    @Test
    void whenUpdateNamespace_thenSuccess() {
        // Given
        KeyPair keyPair = createTestKeyPair();
        KeyPair newKeyPair = createTestKeyPair();
        Namespace namespace = Namespace.builder()
                .namespaceId("test-namespace")
                .namespaceName("Original Name")
                .description("Original Description")
                .keyPairId(keyPair.getKeyPairId())
                .build();
        namespaceRepository.save(namespace);

        // When
        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "namespaceName": "Updated Name",
                    "description": "Updated Description",
                    "keyPairId": %d
                }
                """, newKeyPair.getKeyPairId()))
                .when()
                .put("/namespace/test-namespace")
                .then()
                .statusCode(200)
                .body("namespaceName", equalTo("Updated Name"))
                .body("description", equalTo("Updated Description"))
                .body("keyPairId", equalTo(newKeyPair.getKeyPairId().intValue()));
    }

    @Test
    void whenUpdateNonExistentNamespace_thenFail() {
        KeyPair keyPair = createTestKeyPair();

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "namespaceName": "Updated Name",
                    "description": "Updated Description",
                    "keyPairId": %d
                }
                """, keyPair.getKeyPairId()))
                .when()
                .put("/namespace/non-existent")
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("NAMESPACE_003_400"))
                .body("shortDescriptions", equalTo("Namespace not existed"));
    }

    @Test
    void whenIndexNamespaces_thenSuccess() {
        // Given
        KeyPair keyPair = createTestKeyPair();
        List<Namespace> namespaces = List.of(
                Namespace.builder()
                        .namespaceId("namespace-1")
                        .namespaceName("Test Namespace 1")
                        .description("Description 1")
                        .keyPairId(keyPair.getKeyPairId())
                        .build(),
                Namespace.builder()
                        .namespaceId("namespace-2")
                        .namespaceName("Test Namespace 2")
                        .description("Description 2")
                        .keyPairId(keyPair.getKeyPairId())
                        .build()
        );
        namespaceRepository.saveAll(namespaces);

        // When/Then
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/namespace/index")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("findAll { it.namespaceName.startsWith('Test Namespace') }", hasSize(2));
    }

    @Test
    void whenIndexNamespacesWithNameFilter_thenSuccess() {
        // Given
        KeyPair keyPair = createTestKeyPair();
        List<Namespace> namespaces = List.of(
                Namespace.builder()
                        .namespaceId("namespace-1")
                        .namespaceName("Admin Namespace")
                        .keyPairId(keyPair.getKeyPairId())
                        .build(),
                Namespace.builder()
                        .namespaceId("namespace-2")
                        .namespaceName("User Namespace")
                        .keyPairId(keyPair.getKeyPairId())
                        .build()
        );
        namespaceRepository.saveAll(namespaces);

        // When/Then
        given()
                .queryParam("namespaceName", "Admin Namespace")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/namespace/index")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].namespaceName", equalTo("Admin Namespace"));
    }
}