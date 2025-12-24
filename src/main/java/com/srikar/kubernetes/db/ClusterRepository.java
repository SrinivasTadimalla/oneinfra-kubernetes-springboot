package com.srikar.kubernetes.db;

import com.srikar.kubernetes.entity.ClusterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClusterRepository extends JpaRepository<ClusterEntity, UUID> {
    Optional<ClusterEntity> findByName(String name);
}
