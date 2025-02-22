package org.ducanh.apiiam.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateGroupRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateGroupRequestDto;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.ducanh.apiiam.services.GroupService;
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
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(
            @RequestBody CreateGroupRequestDto request,
            @PathVariable String namespaceId) {
        log.info("Creating group: {}", request);
        GroupResponseDto response = groupService.createGroup(namespaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("group-id/{id}")
    public ResponseEntity<GroupResponseDto> getGroup(@PathVariable String id,
                                                     @PathVariable String namespaceId) {
        GroupResponseDto response = groupService.getGroup(namespaceId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/index")
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
    public ResponseEntity<GroupResponseDto> updateGroup(
            @PathVariable String id,
            @PathVariable String namespaceId,
            @RequestBody UpdateGroupRequestDto request) {
        GroupResponseDto response = groupService.updateGroup(namespaceId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("group-id/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String namespaceId,
                                            @PathVariable String groupId) {
        groupService.deleteGroup(namespaceId, groupId);
        return ResponseEntity.noContent().build();
    }
}
