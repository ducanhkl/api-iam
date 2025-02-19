package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.ducanh.apiiam.dto.responses.UserGroupResponseDto;
import org.ducanh.apiiam.dto.responses.VerifyUserGroupResponseDto;
import org.ducanh.apiiam.entities.Group;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.entities.UserGroup;
import org.ducanh.apiiam.repositories.GroupRepository;
import org.ducanh.apiiam.repositories.UserGroupRepository;
import org.ducanh.apiiam.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public UserGroupService(
            UserGroupRepository userGroupRepository,
            GroupRepository groupRepository,
            UserRepository userRepository
    ) {
        this.userGroupRepository = userGroupRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void assignGroupsToUser(Long userId, List<String> groupIds) {
        User user = userRepository.findByUserIdOrThrow(userId);
        String namespaceId = user.getNamespaceId();
        if (!groupRepository.existsAllByNamespaceIdAndGroupIdIn(namespaceId, groupIds)) {
            throw new RuntimeException("One or more groups not found");
        }
        List<String> notIncludedGroup = userGroupRepository.findGroupIdsByUserIdAndGroupIdNotIn(userId, groupIds);
        List<Group> groupsToAssign = groupRepository.findAllById(notIncludedGroup);
        List<UserGroup> newUserGroups = groupsToAssign.stream()
                .map(group -> UserGroup.builder()
                        .userId(userId)
                        .groupId(group.getGroupId())
                        .namespaceId(group.getNamespaceId())
                        .build())
                .collect(Collectors.toList());
        userGroupRepository.saveAll(newUserGroups);
    }

    public Page<UserGroupResponseDto> getUserGroups(Long userId, String groupName, Boolean assignedOnly, Pageable pageable) {
        Page<Group> result = groupRepository.findAll(buildSpecToFindGroupByUserId(userId, groupName, assignedOnly), pageable);
        List<String> groupIds = result.stream().map(Group::getGroupId).toList();
        if (assignedOnly) {
            return result.map(group -> group.userGroupResponseDto(true));
        }
        List<String> groupIdsExisted = userGroupRepository.findAllUserGroupsByUserIdAndGroupIdIn(userId, groupIds)
                .stream().map(UserGroup::getGroupId).toList();
        Set<String> groupIdsAssignedSet = new HashSet<>(groupIdsExisted);
        return result.map(group -> group.userGroupResponseDto(groupIdsAssignedSet.contains(group.getGroupId())));
    }

    public List<VerifyUserGroupResponseDto> verifyUserGroups(Long userId, List<String> groupIds) {
        userRepository.findByUserIdOrThrow(userId);
        List<UserGroup> userGroups = userGroupRepository.findAllUserGroupsByUserIdAndGroupIdIn(userId, groupIds);
        return userGroups.stream()
                .map((userGroup) -> new VerifyUserGroupResponseDto(userGroup.getGroupId(), userGroup.getAssignedAt()))
                .toList();
    }

    public void removeUserFromGroups(Long userId, List<String> groupIds) {
        userRepository.findByUserIdOrThrow(userId);
        Integer numberOfDeletedRecords = userGroupRepository.removeUserFromGroups(userId, groupIds);
        log.info(MessageFormat.format("Remove {0} groups from user: {1}", numberOfDeletedRecords, userId));
    }

    private Specification<Group> buildSpecToFindGroupByUserId(Long userId, String groupName, Boolean assignedOnly) {
        return (root, query, cb) -> {
            assert query != null;
            List<Predicate> predicates = new ArrayList<>();
            if (assignedOnly) {
                Subquery<String> userGroupSubquery = query.subquery(String.class);
                Root<UserGroup> userGroupRoot = userGroupSubquery.from(UserGroup.class);
                userGroupSubquery.select(userGroupRoot.get(UserGroup.Fields.groupId))
                        .where(cb.equal(userGroupRoot.get(UserGroup.Fields.userId), userId));
                predicates.add(root.get(Group.Fields.groupId).in(userGroupSubquery));
            }
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
