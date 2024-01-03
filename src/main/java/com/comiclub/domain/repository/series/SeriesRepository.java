package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.sereis.Series;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeriesRepository extends JpaRepository<Series, Long>, JpaSeriesRepository {

    @Query("SELECT s FROM Series s WHERE s.memberId = :memberId AND s.name LIKE %:name%")
    Slice<Series> searchMySeries(@Param("memberId") Long memberId, @Param("name") String name, Pageable pageable);

    @Modifying
    @Query("UPDATE Series s SET s.totalEp = s.totalEp + 1 WHERE s.id = :id")
    void increaseTotalEp(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Series s SET s.totalEp = s.totalEp - 1 WHERE s.id = :id")
    void decreaseTotalEp(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Series s SET s.totalEpBoard = s.totalEpBoard + 1 WHERE s.id = :id")
    void increaseTotalEpBoard(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Series s SET s.totalEpBoard = s.totalEpBoard - 1 WHERE s.id = :id")
    void decreaseTotalEpBoard(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Series s SET s.totalLike = s.totalLike + 1 WHERE s.id = :id")
    void increaseTotalLike(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Series s SET s.totalLike = s.totalLike - 1 WHERE s.id = :id")
    void decreaseTotalLike(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Series s SET s.totalView = s.totalView + 1 WHERE s.id = :id")
    void increaseTotalView(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Series s SET s.totalFollower = s.totalFollower + 1 WHERE s.id = :id")
    void increaseTotalFollower(@Param("id") Long id);
    @Modifying
    @Query("UPDATE Series s SET s.totalFollower = s.totalFollower - 1 WHERE s.id = :id")
    void decreaseTotalFollower(@Param("id") Long id);
}
