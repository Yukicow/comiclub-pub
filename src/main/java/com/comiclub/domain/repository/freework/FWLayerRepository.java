package com.comiclub.domain.repository.freework;

import com.comiclub.domain.entity.freework.FreeWorkLayer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FWLayerRepository extends JpaRepository<FreeWorkLayer, Long> {



    List<FreeWorkLayer> findBySceneId(Long sceneId);

    @Query(value = "UPDATE fw_layer l " +
            "SET l.layer_number = l.layer_number - 1 " +
            "WHERE l.scene_id = :sceneId " +
                "AND l.layer_number > :layerNumber " +
            "ORDER BY l.layer_number ASC", nativeQuery = true)
    void decreaseLayerNumbers(@Param("sceneId") Long sceneId, @Param("layerNumber") Integer layerNumber);

    @EntityGraph(attributePaths = {"scene"})
    Optional<FreeWorkLayer> findWithScenesById(Long layerId);
}
