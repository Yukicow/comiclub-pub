package com.comiclub.domain.repository.board.episode;

import com.comiclub.domain.entity.board.EpisodeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface JpaEpBoardRepository {



    List<EpisodeBoard> findViewerEpisodeBoard(long seriesId, long episodeNumber);

    Optional<EpisodeBoard> findOneByEpisodeId(Long episodeId);



}
