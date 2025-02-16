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
@RequestMapping("/group")
@RequiredArgsConstructor
@Slf4j
public class GroupControllers {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(
            @RequestBody CreateGroupRequestDto request) {
        log.info("Creating group: {}", request);
        GroupResponseDto response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDto> getGroup(@PathVariable String id) {
        GroupResponseDto response = groupService.getGroup(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("index")
    public ResponseEntity<List<GroupResponseDto>> indexGroups(
            @RequestParam(required = false) String groupName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<GroupResponseDto> groupPage = groupService.indexGroups(groupName, pageable);

        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(groupPage.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(groupPage.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(groupPage.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(groupPage.getTotalPages()))
                .body(groupPage.getContent());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponseDto> updateGroup(
            @PathVariable String id,
            @RequestBody UpdateGroupRequestDto request) {
        GroupResponseDto response = groupService.updateGroup(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}
