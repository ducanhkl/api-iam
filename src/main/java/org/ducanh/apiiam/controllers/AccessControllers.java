package org.ducanh.apiiam.controllers;


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
@Tag(name = "Access controllers")
@RequiredArgsConstructor
public class AccessControllers {

    private final AccessService accessService;

    @PostMapping("/check-access")
    public ResponseEntity<CheckAccessResponse> checkAccess(
            @RequestBody @Valid CheckAccessRequest checkAccessRequest,
            @RequestHeader(value = "namespace-id") String namespaceId
    ) {
        return ResponseEntity.ok(accessService.checkAccess(checkAccessRequest, namespaceId));
    }
}
