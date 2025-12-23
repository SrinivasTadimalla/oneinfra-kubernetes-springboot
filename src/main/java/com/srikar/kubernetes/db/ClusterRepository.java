package com.srikar.kubernetes.db;

import com.srikar.kubernetes.entity.ClusterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClusterRepository extends JpaRepository<ClusterEntity, Long> {
    Optional<ClusterEntity> findByName(String name);
}
