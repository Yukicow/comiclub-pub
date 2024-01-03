package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.sereis.Series;
import com.comiclub.web.contoller.board.OrderType;
import com.comiclub.web.contoller.board.series.dto.SeriesSearchCond;
import com.comiclub.web.contoller.board.series.SeriesSearchType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.comiclub.domain.entity.sereis.QSeries.*;


@Repository
public class JpaSeriesRepositoryImpl implements JpaSeriesRepository {


    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    private final int MIN_REQUIRED_TOTAL_BOARD = 0;

    public JpaSeriesRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Page<Series> searchSeries(SeriesSearchCond cond, Pageable pageable) {

        List<Series> seriesList = queryFactory
                .selectFrom(series)
                .where(
                        searchCond(cond),
                        cond.getViewAdult() ? null : series.adultOnly.isFalse(),
                        series.totalEpBoard.gt(MIN_REQUIRED_TOTAL_BOARD),
                        series.pub.isTrue()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(seriesOrder(cond))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(series.count())
                .from(series)
                .where(
                        searchCond(cond),
                        cond.getViewAdult() ? null : series.adultOnly.isFalse(),
                        series.totalEpBoard.gt(MIN_REQUIRED_TOTAL_BOARD),
                        series.pub.isTrue()
                );

        return PageableExecutionUtils.getPage(seriesList, pageable, () -> countQuery.fetchOne());
    }

    private BooleanExpression searchCond(SeriesSearchCond cond) {
        String keyword = cond.getKeyword();
        if(!StringUtils.hasText(keyword)) return null;

        SeriesSearchType type = cond.getType();
        switch (type) {
            case NAME:
                return series.name.contains(keyword);
            case DESCRIPTION:
                return series.description.contains(keyword);
            case WRITER:
                return series.writer.contains(keyword);
            default:
                return null;
        }
    }

    private OrderSpecifier seriesOrder(SeriesSearchCond cond) {
        OrderType order = cond.getOrder();
        switch (order){
            case BEST:
                return series.totalLike.desc();
            default:
                return series.createdDate.desc();
        }
    }


    @Override
    public List<Series> findWriterSeries(Long memberId, Long seriesId, boolean adultOnly, Pageable pageable) {
        return queryFactory
                .selectFrom(series)
                .where(
                        series.memberId.eq(memberId),
                        series.id.ne(seriesId),
                        adultOnly ? null : series.adultOnly.eq(false),
                        series.totalEpBoard.gt(MIN_REQUIRED_TOTAL_BOARD)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(series.totalLike.desc())
                .fetch();
    }

    @Override
    public List<Series> findDayBestSeries() {
        return queryFactory
                .selectFrom(series)
                .where(
                        series.createdDate.after(LocalDateTime.now().minusDays(1)),
                        series.totalEpBoard.gt(MIN_REQUIRED_TOTAL_BOARD),
                        series.pub.isTrue()
                )
                .orderBy(series.totalLike.desc())
                .limit(10)
                .fetch();
    }

    @Override
    public List<Series> findWeekBestSeries() {
        return queryFactory
                .selectFrom(series)
                .where(
                        series.createdDate.after(LocalDateTime.now().minusWeeks(1)),
                        series.totalEpBoard.gt(MIN_REQUIRED_TOTAL_BOARD),
                        series.pub.isTrue()
                )
                .orderBy(series.totalLike.desc())
                .limit(10)
                .fetch();
    }

    @Override
    public List<Series> findRankSeries() {
        return queryFactory
                .selectFrom(series)
                .orderBy(series.totalLike.desc())
                .where(
                        series.totalEpBoard.gt(MIN_REQUIRED_TOTAL_BOARD),
                        series.pub.isTrue()
                )
                .limit(10)
                .fetch();
    }


    @Override
    public Page<Series> adminSearchSeries(SeriesSearchCond cond, Pageable pageable) {

        List<Series> seriesList = queryFactory
                .selectFrom(series)
                .where(searchCond(cond))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(seriesOrder(cond))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(series.count())
                .from(series)
                .where(searchCond(cond));

        return PageableExecutionUtils.getPage(seriesList, pageable, () -> countQuery.fetchOne());
    }


}
