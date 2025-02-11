package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.KeyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyPairRepository extends JpaRepository<KeyPair, Long> {

    KeyPair findKeyPairsByKeyPairId(Long keyPairId);
}
