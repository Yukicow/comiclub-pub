package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.sereis.Series;
import com.comiclub.web.contoller.board.series.dto.SeriesSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JpaSeriesRepository {
    Page<Series> searchSeries(SeriesSearchCond cond, Pageable pageable);

    List<Series> findWriterSeries(Long memberId, Long seriesId, boolean adultOnly, Pageable pageable);

    List<Series> findDayBestSeries();
    List<Series> findWeekBestSeries();
    List<Series> findRankSeries();



    /**
     * Admin
     * */
    Page<Series> adminSearchSeries(SeriesSearchCond cond, Pageable pageable);
}
