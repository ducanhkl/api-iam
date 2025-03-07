package org.ducanh.apiiam.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateNamespaceRequestDto;
import org.ducanh.apiiam.dto.requests.IndexNamespaceRequestParamsDto;
import org.ducanh.apiiam.dto.requests.UpdateNamespaceRequestDto;
import org.ducanh.apiiam.dto.responses.NamespaceResponseDto;
import org.ducanh.apiiam.services.NamespaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.ducanh.apiiam.Constants.*;


@RestController
@RequestMapping("/namespace")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Namespace controller")
public class NamespaceController {

    private final NamespaceService namespaceService;

    @PostMapping
    @Operation(summary = "Create namespace")
    public ResponseEntity<NamespaceResponseDto> createNamespace(
            @Valid @RequestBody CreateNamespaceRequestDto request) {
        log.info("Creating namespace: {}", request);
        NamespaceResponseDto response = namespaceService.createNamespace(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{namespaceId}")
    public ResponseEntity<NamespaceResponseDto> getNamespace(@PathVariable String namespaceId) {
        NamespaceResponseDto response = namespaceService.getNamespace(namespaceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/index")
    public ResponseEntity<List<NamespaceResponseDto>> indexNamespaces(
            IndexNamespaceRequestParamsDto params) {
        Pageable pageable = PageRequest.of(params.page(), params.size());
        Page<NamespaceResponseDto> namespacePage = namespaceService.indexNamespaces(params, pageable);

        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(namespacePage.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(namespacePage.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(namespacePage.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(namespacePage.getTotalPages()))
                .body(namespacePage.getContent());
    }

    @PutMapping("/{namespaceId}")
    public ResponseEntity<NamespaceResponseDto> updateNamespace(
            @PathVariable String namespaceId,
            @Valid @RequestBody UpdateNamespaceRequestDto request) {
        NamespaceResponseDto response = namespaceService.updateNamespace(namespaceId, request);
        return ResponseEntity.ok(response);
    }
}
