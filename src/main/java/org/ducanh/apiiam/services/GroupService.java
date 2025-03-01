package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateGroupRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateGroupRequestDto;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.ducanh.apiiam.entities.Group;
import org.ducanh.apiiam.exceptions.CommonException;
import org.ducanh.apiiam.exceptions.ErrorCode;
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
                .orElseThrow(() -> new CommonException(ErrorCode.GROUP_NOT_FOUND,
                        "GroupId {0}, namespace: {1} not found", groupId, namespaceId));
        group.setGroupName(request.groupName());
        group.setDescription(request.description());
        return group.toGroupResponseDto();
    }

    public GroupResponseDto getGroup(String namespaceId,
                                     String groupId) {
        return groupRepository.findGroupByNamespaceIdAndGroupId(namespaceId, groupId)
                .map(Group::toGroupResponseDto)
                .orElseThrow(() -> new CommonException(ErrorCode.GROUP_NOT_FOUND,
                        "GroupId {0}, namespace: {1} not found", groupId, namespaceId));
    }

    public Page<GroupResponseDto> indexGroups(String namespaceId, String groupName, Pageable pageable) {
        return groupRepository.findAll(buildSearchCriteria(namespaceId, groupName), pageable)
                .map(Group::toGroupResponseDto);
    }

    public void deleteGroup(String namespaceId, String groupId) {
        Group group = groupRepository.findGroupByNamespaceIdAndGroupId(namespaceId, groupId)
                .orElseThrow(() -> new CommonException(ErrorCode.GROUP_NOT_FOUND,
                        "GroupId {0}, namespace: {1} not found", groupId, namespaceId));
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
