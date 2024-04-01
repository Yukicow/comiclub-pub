package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.freework.FreeWorkLayer;
import com.comiclub.domain.entity.sereis.EpisodeLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EpLayerRepository extends JpaRepository<EpisodeLayer, Long> {


    List<EpisodeLayer> findBySceneId(Long sceneId);

    @Query(value = """
            UPDATE ep_layer l 
            SET l.layer_number = l.layer_number - 1 
            WHERE 
                l.ep_scene_id = :sceneId 
                AND l.layer_number > :layerNumber 
            ORDER BY l.layer_number ASC
            """, nativeQuery = true)
    void decreaseLayerNumbers(@Param("sceneId") Long sceneId, @Param("layerNumber") Integer layerNumber);
}
