package org.ducanh.apiiam.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateGroupRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateGroupRequestDto;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.ducanh.apiiam.services.GroupService;
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
@RequestMapping("/group/namespace-id/{namespaceId}")
@Tag(name = "Group Controller", description = "Operations related to group management within a namespace")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;
    private final NamespaceService namespaceService;

    @PostMapping
    @Operation(summary = "Create a new group", description = "Creates a new group within the specified namespace.")
    public ResponseEntity<GroupResponseDto> createGroup(
            @RequestBody CreateGroupRequestDto request,
            @PathVariable String namespaceId) {
        log.info("Creating group: {}", request);
        GroupResponseDto response = groupService.createGroup(namespaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("group-id/{id}")
    @Operation(summary = "Get group details", description = "Retrieves details of a specific group by its ID within the specified namespace.")
    public ResponseEntity<GroupResponseDto> getGroup(@PathVariable String id,
                                                     @PathVariable String namespaceId) {
        GroupResponseDto response = groupService.getGroup(namespaceId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/index")
    @Operation(summary = "List groups", description = "Lists all groups within the specified namespace, with optional pagination and filtering by group name.")
    public ResponseEntity<List<GroupResponseDto>> indexGroups(
            @RequestParam(required = false) String groupName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String namespaceId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<GroupResponseDto> groupPage = groupService.indexGroups(namespaceId, groupName, pageable);

        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(groupPage.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(groupPage.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(groupPage.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(groupPage.getTotalPages()))
                .body(groupPage.getContent());
    }

    @PutMapping("group-id/{id}")
    @Operation(summary = "Update group details", description = "Updates the details of a specific group by its ID within the specified namespace.")
    public ResponseEntity<GroupResponseDto> updateGroup(
            @PathVariable String id,
            @PathVariable String namespaceId,
            @RequestBody UpdateGroupRequestDto request) {
        GroupResponseDto response = groupService.updateGroup(namespaceId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("group-id/{groupId}")
    @Operation(summary = "Delete a group", description = "Deletes a specific group by its ID within the specified namespace and increases the namespace version.")
    public ResponseEntity<Void> deleteGroup(@PathVariable String namespaceId,
                                            @PathVariable String groupId) {
        groupService.deleteGroup(namespaceId, groupId);
        namespaceService.increaseNamespaceVersion(namespaceId);
        return ResponseEntity.noContent().build();
    }
}