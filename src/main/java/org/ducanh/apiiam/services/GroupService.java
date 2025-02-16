package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateGroupRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateGroupRequestDto;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.ducanh.apiiam.entities.Group;
import org.ducanh.apiiam.repositories.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public GroupResponseDto createGroup(CreateGroupRequestDto request) {
        groupRepository.notExistsByNamespaceIdAndGroupIdOrThrow(request.groupId(), request.namespaceId());
        Group group = Group.builder()
                .groupName(request.groupName())
                .description(request.description())
                .namespaceId(request.namespaceId())
                .groupId(request.groupId())
                .build();

        Group savedGroup = groupRepository.save(group);
        return savedGroup.toGroupResponseDto();
    }

    @Transactional
    public GroupResponseDto updateGroup(String id, UpdateGroupRequestDto request) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
        group.setGroupName(request.groupName());
        group.setDescription(request.description());
        // updatedAt is automatically handled by @UpdateTimestamp
        return group.toGroupResponseDto();
    }

    public GroupResponseDto getGroup(String id) {
        return groupRepository.findById(id)
                .map(Group::toGroupResponseDto)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
    }

    public Page<GroupResponseDto> indexGroups(String namespaceId, String groupName, Pageable pageable) {
        return groupRepository.findAll(buildSearchCriteria(namespaceId, groupName), pageable)
                .map(Group::toGroupResponseDto);
    }

    public void deleteGroup(String id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
        groupRepository.delete(group);
    }

    private Specification<Group> buildSearchCriteria(String namespaceId, String groupName) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(Group.Fields.namespaceId), namespaceId));
            if (StringUtils.hasText(groupName)) {
                predicates.add(cb.like(
                        cb.lower(root.get(Group.Fields.groupName)),
                        "%" + groupName.toLowerCase().trim() + "%"
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
