package org.ducanh.apiiam.controllers;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("access")
@Tag(name = "Access controllers")
public class AccessControllers {

    private final AccessService accessService;

    @PostMapping("/check-access")
    public ResponseEntity<CheckAcecssResponse> checkAccess(
        @RequestBody @Valid CheckAccessRequest,
        @RequestHeader(value = "namespace-id") String namespaceId
    ) {

    }

}
