package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateNamespaceRequestDto;
import org.ducanh.apiiam.dto.requests.IndexNamespaceRequestParamsDto;
import org.ducanh.apiiam.dto.requests.UpdateNamespaceRequestDto;
import org.ducanh.apiiam.dto.responses.NamespaceResponseDto;
import org.ducanh.apiiam.entities.Namespace;
import org.ducanh.apiiam.repositories.KeyPairRepository;
import org.ducanh.apiiam.repositories.NamespaceRepository;
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
public class NamespaceService {
    private final NamespaceRepository namespaceRepository;
    private final KeyPairRepository keyPairRepository;

    public NamespaceService(NamespaceRepository namespaceRepository, KeyPairRepository keyPairRepository) {
        this.namespaceRepository = namespaceRepository;
        this.keyPairRepository = keyPairRepository;
    }

    public NamespaceResponseDto createNamespace(CreateNamespaceRequestDto request) {
        // Check for duplicate namespace name
        if (namespaceRepository.existsByNamespaceName(request.namespaceName())) {
            throw new RuntimeException("Namespace with name " + request.namespaceName() + " already exists");
        }

        // Check if keyPair exists
        keyPairRepository.findById(request.keyPairId())
                .orElseThrow(() -> new RuntimeException("KeyPair not found with id: " + request.keyPairId()));

        Namespace namespace = Namespace.builder()
                .namespaceId(request.namespaceId())
                .namespaceName(request.namespaceName())
                .description(request.description())
                .keyPairId(request.keyPairId())
                .build();

        Namespace savedNamespace = namespaceRepository.save(namespace);
        return savedNamespace.toNamespaceResponseDto();
    }

    @Transactional
    public NamespaceResponseDto updateNamespace(String id, UpdateNamespaceRequestDto request) {
        Namespace namespace = namespaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Namespace not found with id: " + id));

        // Check for duplicate namespace name if it's different from current
        if (!namespace.getNamespaceName().equals(request.namespaceName()) &&
                namespaceRepository.existsByNamespaceName(request.namespaceName())) {
            throw new RuntimeException("Namespace with name " + request.namespaceName() + " already exists");
        }

        // Check if keyPair exists
        keyPairRepository.findById(request.keyPairId())
                .orElseThrow(() -> new RuntimeException("KeyPair not found with id: " + request.keyPairId()));

        namespace.setNamespaceName(request.namespaceName());
        namespace.setDescription(request.description());
        namespace.setKeyPairId(request.keyPairId());

        return namespace.toNamespaceResponseDto();
    }

    public NamespaceResponseDto getNamespace(String id) {
        return namespaceRepository.findById(id)
                .map(Namespace::toNamespaceResponseDto)
                .orElseThrow(() -> new RuntimeException("Namespace not found with id: " + id));
    }

    public Page<NamespaceResponseDto> indexNamespaces(IndexNamespaceRequestParamsDto params, Pageable pageable) {
        return namespaceRepository.findAll(buildSearchCriteria(params), pageable)
                .map(Namespace::toNamespaceResponseDto);
    }

    private Specification<Namespace> buildSearchCriteria(IndexNamespaceRequestParamsDto params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(params.namespaceName())) {
                predicates.add(cb.equal(
                        cb.lower(root.get(Namespace.Fields.namespaceName)),
                        params.namespaceName().toLowerCase().trim()
                ));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
