package org.ducanh.apiiam.services;

import org.ducanh.apiiam.dto.requests.CheckAccessRequest;
import org.ducanh.apiiam.dto.responses.CheckAccessResponse;
import org.ducanh.apiiam.exceptions.CommonException;
import org.ducanh.apiiam.exceptions.ErrorCode;
import org.ducanh.apiiam.storage.PolicyStorage;
import org.ducanh.apiiam.storage.PolicyStorageManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AccessService {

    private final PolicyStorageManagement policyStorageManagement;

    @Autowired
    public AccessService(final PolicyStorageManagement policyStorageManagement) {
        this.policyStorageManagement = policyStorageManagement;
    }

    public CheckAccessResponse checkAccess(
            CheckAccessRequest checkAccessRequest,
            String namespaceId
    ) {
        PolicyStorage policyStorage = policyStorageManagement.getPolicyStorage(namespaceId);
        if (Objects.isNull(policyStorage)) {
            throw new CommonException(ErrorCode.NAMESPACE_NOT_EXISTED, "NamespaceId: {0} not existed", namespaceId);
        }
        for (String groupId : checkAccessRequest.groupId()) {
            if (policyStorage.checkAccess(groupId, checkAccessRequest.permissionId())) {
                return new CheckAccessResponse(true);
            }
        }
        return new CheckAccessResponse(false);
    }
}
