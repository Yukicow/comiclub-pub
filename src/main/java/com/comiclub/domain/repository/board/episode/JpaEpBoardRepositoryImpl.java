package com.comiclub.domain.repository.board.episode;

import com.comiclub.domain.entity.board.EpisodeBoard;
import com.comiclub.domain.repository.board.episode.JpaEpBoardRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.comiclub.domain.entity.board.QEpisodeBoard.*;
import static com.comiclub.domain.entity.sereis.QSeries.series;


@Repository
public class JpaEpBoardRepositoryImpl implements JpaEpBoardRepository {


    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public JpaEpBoardRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<EpisodeBoard> findViewerEpisodeBoard(long seriesId, long episodeNumber) {
        long offset = episodeNumber - 1;
        boolean hasPrevEpisode = offset > 0;
        return queryFactory
                    .selectFrom(episodeBoard)
                    .where(
                            episodeBoard.seriesId.eq(seriesId),
                            episodeBoard.pub.isTrue()
                    )
                    .orderBy(episodeBoard.episodeNumber.asc())
                    .offset(hasPrevEpisode ? offset - 1 : 0) // 이전 페이지가 없으면 0
                    .limit(hasPrevEpisode ? 3 : 2) // 이전 페이지가 있으면 이전, 현재, 다음 총 3개, 없으면 현재, 다음 총 2개;
                    .fetch();
    }

    @Override
    public Optional<EpisodeBoard> findOneByEpisodeId(Long episodeId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(episodeBoard)
                        .where(episodeBoard.episodeId.eq(episodeId))
                        .fetchFirst()
        );
    }


}
