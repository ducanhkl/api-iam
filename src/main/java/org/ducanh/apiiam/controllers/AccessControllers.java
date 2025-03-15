package org.ducanh.apiiam.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ducanh.apiiam.dto.requests.CheckAccessRequest;
import org.ducanh.apiiam.dto.responses.CheckAccessResponse;
import org.ducanh.apiiam.services.AccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("access")
@Tag(name = "Access controllers", description = "Operations related to access control")
@RequiredArgsConstructor
public class AccessControllers {

    private final AccessService accessService;

    @PostMapping("/check-access")
    @Operation(summary = "Check access", description = "Checks if a user has access based on the provided request and namespace ID")
    public ResponseEntity<CheckAccessResponse> checkAccess(
            @RequestBody @Valid CheckAccessRequest checkAccessRequest,
            @RequestHeader(value = "namespace-id")
            @Parameter(description = "Namespace ID for access control", required = true) String namespaceId
    ) {
        return ResponseEntity.ok(accessService.checkAccess(checkAccessRequest, namespaceId));
    }
}