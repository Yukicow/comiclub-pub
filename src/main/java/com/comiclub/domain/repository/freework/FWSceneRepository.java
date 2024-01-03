package com.comiclub.domain.repository.freework;

import com.comiclub.domain.entity.freework.FreeWorkScene;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FWSceneRepository extends JpaRepository<FreeWorkScene, Long> {


    @EntityGraph(attributePaths = {"layers"})
    Optional<FreeWorkScene> findWithLayersById(@Param("sceneId") Long sceneId);

    @Query(value = "SELECT s FROM FreeWorkScene s " +
            "WHERE s.freeWork.id = :workId AND s.sceneNumber BETWEEN :sceneNum - 1 AND :sceneNum + 1")
    List<FreeWorkScene> findScenesByRangeNumber(@Param("workId") Long workId, @Param("sceneNum") int sceneNum);


    /**
     * JPQL Update쿼리에는 ORDER BY를 사용할 수 없기 때문에 native query로 해결
     * ( update를 하는 순서가 여기서는 중요하기 때문, 컬럼의 유니크 제약 조건을 위배하지 않게 하기 위함  )
     * 단, native query가 되었기 때문에 해당 method를 사용할 때에는 1차 캐시를 적절하게 비워 주어야 함
     * */
    @Query(value = "UPDATE fw_scene s " +
            "SET s.scene_number = s.scene_number + 1 " +
            "WHERE s.free_work_id = :freeWorkId AND s.scene_number >= :sceneNumber " +
            "ORDER BY s.scene_number DESC", nativeQuery = true)
    void increaseSceneNumbers(@Param("freeWorkId") Long freeWorkId, @Param("sceneNumber") Integer sceneNumber);

    @Query(value = "UPDATE fw_scene s " +
            "SET s.scene_number = s.scene_number - 1 " +
            "WHERE s.free_work_id = :freeWorkId AND s.scene_number > :sceneNumber " +
            "ORDER BY s.scene_number ASC", nativeQuery = true)
    void decreaseSceneNumbers(@Param("freeWorkId") Long freeWorkId, @Param("sceneNumber") Integer sceneNumber);

    int countByFreeWorkId(Long workId);
}
