package com.comiclub.domain.service.board;


import com.comiclub.domain.entity.histroy.FollowHistory;
import com.comiclub.domain.entity.sereis.Series;
import com.comiclub.domain.repository.board.episode.EBoardRepository;
import com.comiclub.domain.repository.series.FollowHistoryRepository;
import com.comiclub.domain.repository.series.SeriesRepository;
import com.comiclub.web.contoller.board.series.dto.SeriesBoardDto;
import com.comiclub.web.contoller.board.series.dto.SeriesSearchCond;
import com.comiclub.web.exception.AdultOnlyAccessibleException;
import com.comiclub.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SeriesService {

    private final FollowHistoryRepository followHistoryRepository;

    private final SeriesRepository seriesRepository;
    private final EBoardRepository eBoardRepository;


    @Transactional(readOnly = true)
    public Page<SeriesBoardDto> searchSeries(SeriesSearchCond cond, Pageable pageable) {
        Page<Series> page = seriesRepository.searchSeries(cond, pageable);
        List<SeriesBoardDto> seriesBoardDtos = page.stream()
                .map(series -> new SeriesBoardDto(series))
                .collect(Collectors.toList());

        return  PageableExecutionUtils.getPage(seriesBoardDtos, pageable, () -> page.getTotalElements());
    }


    public String followSeries(Long memberId, Long seriesId, boolean adult) {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("There is no series id with '" + seriesId + "'"));

        if(series.getAdultOnly() && !adult)
            throw new AdultOnlyAccessibleException("Series is adult only");

        FollowHistory history = followHistoryRepository.findByMemberIdAndSeriesId(memberId, seriesId);

        if(history == null){
            FollowHistory followHistory = FollowHistory.createNewFollowHistory(memberId, seriesId, series.getAdultOnly());
            followHistoryRepository.save(followHistory);
            seriesRepository.increaseTotalFollower(seriesId);
            return "FOLLOW";
        }
        else{
            followHistoryRepository.delete(history);
            seriesRepository.decreaseTotalFollower(seriesId);
            return "UNFOLLOW";
        }

    }


    public void closePub(Series series) {
        series.changePub(false);
    }

    public void deleteSeries(Series series) {
        seriesRepository.delete(series);
    }

    public void increaseTotalView(Long seriesId, Long epBoardId){
        seriesRepository.increaseTotalView(seriesId);
        eBoardRepository.increaseTotalView(epBoardId);
    }



}
