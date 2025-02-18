package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
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

import java.util.ArrayList;
import java.util.List;
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

    public Page<GroupResponseDto> getUserGroups(Long userId, String groupName, Pageable pageable) {
        return groupRepository.findAll(buildSpecToFindGroupByUserId(userId, groupName), pageable)
                .map(Group::toGroupResponseDto);
    }

    public List<VerifyUserGroupResponseDto> verifyUserGroups(Long userId, List<String> groupIds) {
        User user = userRepository.findByUserIdOrThrow(userId);
        String namespaceId = user.getNamespaceId();
        List<UserGroup> userGroups = userGroupRepository.findAllUserGroupByUserIdAndNamespaceId(userId, namespaceId);
        return userGroups.stream()
                .map((userGroup) -> new VerifyUserGroupResponseDto(userGroup.getGroupId(), userGroup.getAssignedAt()))
                .toList();
    }

    public Page<GroupResponseDto> getUserGroupsNotBelongToUser(Long userId, String groupName, Pageable pageable) {
        User user = userRepository.findByUserIdOrThrow(userId);
        String namespaceId = user.getNamespaceId();
        return groupRepository.findAll(buildSpecToFindGroupNotBelongToUser(userId, namespaceId, groupName), pageable)
                .map(Group::toGroupResponseDto);
    }

    private Specification<Group> buildSpecToFindGroupNotBelongToUser(Long userId, String namespaceId, String groupName) {
        return (root, query, cb) -> {
            assert query != null;
            List<Predicate> predicates = new ArrayList<>();
            Subquery<String> userGroupSubquery = query.subquery(String.class);
            Root<UserGroup> userGroupRoot = userGroupSubquery.from(UserGroup.class);
            userGroupSubquery.select(userGroupRoot.get(UserGroup.Fields.groupId))
                    .where(cb.equal(userGroupRoot.get(UserGroup.Fields.userId), userId));
            predicates.add(cb.not(root.get(Group.Fields.groupId).in(userGroupSubquery)));
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

    private Specification<Group> buildSpecToFindGroupByUserId(Long userId, String groupName) {
        return (root, query, cb) -> {
            assert query != null;
            List<Predicate> predicates = new ArrayList<>();
            Subquery<String> userGroupSubquery = query.subquery(String.class);
            Root<UserGroup> userGroupRoot = userGroupSubquery.from(UserGroup.class);
            userGroupSubquery.select(userGroupRoot.get(UserGroup.Fields.groupId))
                    .where(cb.equal(userGroupRoot.get(UserGroup.Fields.userId), userId));
            predicates.add(root.get(Group.Fields.groupId).in(userGroupSubquery));
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
