package com.srikar.kubernetes.db;

import com.srikar.kubernetes.entity.ClusterNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ClusterNodeRepository extends JpaRepository<ClusterNodeEntity, UUID> {

    @Transactional
    void deleteByClusterId(UUID clusterId);
}
