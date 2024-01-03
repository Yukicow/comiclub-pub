package com.comiclub.domain.repository.board.episode;

import com.comiclub.domain.entity.board.EpisodeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EBoardRepository extends JpaRepository<EpisodeBoard, Long>, JpaEpBoardRepository {


    @Query("SELECT b FROM EpisodeBoard b WHERE b.seriesId = :seriesId AND b.pub = true")
    Page<EpisodeBoard> findPubBoardBySeriesId(@Param("seriesId") Long seriesId, Pageable pageable);

    boolean existsByEpisodeId(Long workId);

    @Modifying
    @Query(value = "UPDATE EpisodeBoard e SET e.totalComment = e.totalComment + 1 WHERE e.id = :id")
    void increaseTotalComment(@Param("id") Long id);
    @Modifying
    @Query(value = "UPDATE EpisodeBoard e SET e.totalComment = e.totalComment - :amount WHERE e.id = :id")
    void decreaseTotalComment(@Param("id") Long id, @Param("amount") Long amount);
    @Modifying
    @Query(value = "UPDATE EpisodeBoard e SET totalLike = totalLike + 1 WHERE e.id = :id")
    void increaseTotalLike(@Param("id") Long id);
    @Modifying
    @Query(value = "UPDATE EpisodeBoard e SET totalLike = totalLike - 1 WHERE e.id = :id")
    void decreaseTotalLike(@Param("id") Long id);

    @Modifying
    @Query("UPDATE EpisodeBoard e SET e.totalView = e.totalView + 1 WHERE e.id = :id")
    void increaseTotalView(@Param("id") Long id);
}
