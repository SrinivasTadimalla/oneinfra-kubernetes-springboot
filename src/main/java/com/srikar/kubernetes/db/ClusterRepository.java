package com.srikar.kubernetes.db;

import com.srikar.kubernetes.entity.ClusterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClusterRepository extends JpaRepository<ClusterEntity, UUID> {

    Optional<ClusterEntity> findByName(String name);

    /**
     * Load clusters along with their nodes in a single query
     * to avoid LazyInitializationException during JSON serialization.
     */
    @Query("select distinct c from ClusterEntity c left join fetch c.nodes")
    List<ClusterEntity> findAllWithNodes();

}
