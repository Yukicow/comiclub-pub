package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.sereis.EpisodeScene;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpSceneRepository extends JpaRepository<EpisodeScene, Long>{




    @EntityGraph(attributePaths = {"layers"})
    Optional<EpisodeScene> findWithLayersById(@Param("sceneId") Long sceneId);

    @Query(value = "SELECT s FROM EpisodeScene s " +
            "WHERE s.episode.id = :episodeId AND s.sceneNumber BETWEEN :sceneNum - 1 AND :sceneNum + 1")
    List<EpisodeScene> findScenesByRangeNumber(@Param("episodeId") Long episodeId, @Param("sceneNum") int sceneNum);



    /**
     * JPQL Update쿼리에는 ORDER BY를 사용할 수 없기 때문에 native query로 해결
     * ( update를 하는 순서가 여기서는 중요하기 때문, 컬럼의 유니크 제약 조건을 위배하지 않게 하기 위함  )
     * 단, native query가 되었기 때문에 해당 method를 사용할 때에는 1차 캐시를 적절하게 비워 주어야 함
     * */
    @Query(value = "UPDATE ep_scene s " +
            "SET s.scene_number = s.scene_number + 1 " +
            "WHERE s.episode_id = :episodeId AND s.scene_number >= :sceneNumber " +
            "ORDER BY s.scene_number DESC", nativeQuery = true)
    void increaseSceneNumbers(@Param("episodeId") Long episodeId, @Param("sceneNumber") Integer sceneNumber);

    @Query(value = "UPDATE ep_scene s " +
            "SET s.scene_number = s.scene_number - 1 " +
            "WHERE s.episode_id = :episodeId AND s.scene_number > :sceneNumber " +
            "ORDER BY s.scene_number ASC", nativeQuery = true)
    void decreaseSceneNumbers(@Param("episodeId") Long episodeId, @Param("sceneNumber") Integer sceneNumber);


}
