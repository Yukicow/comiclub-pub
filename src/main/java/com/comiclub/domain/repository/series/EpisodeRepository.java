package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.sereis.Episode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    List<Episode> findBySeriesId(Long seriesId);
    Page<Episode> findBySeriesId(Long seriesId, Pageable pageable);


    @EntityGraph(attributePaths = {"bgSounds"})
    Optional<Episode> findWithBgSoundsById(Long episodeId);

    @EntityGraph(attributePaths = {"scenes"})
    Optional<Episode> findWithScenesById(Long episodeId);

}
