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
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.ducanh.apiiam.helpers.ValidationHelpers.valArg;

@Service
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public GroupResponseDto createGroup(String namespaceId, CreateGroupRequestDto request) {
        groupRepository.notExistsByNamespaceIdAndGroupIdOrThrow(request.groupId(), namespaceId);
        Group group = Group.builder()
                .groupName(request.groupName())
                .description(request.description())
                .namespaceId(namespaceId)
                .groupId(request.groupId())
                .build();

        Group savedGroup = groupRepository.save(group);
        return savedGroup.toGroupResponseDto();
    }

    @Transactional
    public GroupResponseDto updateGroup(String namespaceId, String groupId, UpdateGroupRequestDto request) {
        Group group = groupRepository.findGroupByNamespaceIdAndGroupId(namespaceId, groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        group.setGroupName(request.groupName());
        group.setDescription(request.description());
        return group.toGroupResponseDto();
    }

    public GroupResponseDto getGroup(String namespaceId,
                                     String groupId) {
        return groupRepository.findGroupByNamespaceIdAndGroupId(namespaceId, groupId)
                .map(Group::toGroupResponseDto)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
    }

    public Page<GroupResponseDto> indexGroups(String namespaceId, String groupName, Pageable pageable) {
        return groupRepository.findAll(buildSearchCriteria(namespaceId, groupName), pageable)
                .map(Group::toGroupResponseDto);
    }

    public void deleteGroup(String namespaceId, String id) {
        Group group = groupRepository.findGroupByNamespaceIdAndGroupId(namespaceId, id)
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
