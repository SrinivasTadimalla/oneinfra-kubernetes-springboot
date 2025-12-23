package com.srikar.kubernetes.db;

import com.srikar.kubernetes.entity.ClusterNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClusterNodeRepository extends JpaRepository<ClusterNodeEntity, Long> {
    List<ClusterNodeEntity> findByClusterId(Long clusterId);
    void deleteByClusterId(Long clusterId);
}
