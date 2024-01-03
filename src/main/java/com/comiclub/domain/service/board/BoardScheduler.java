package com.comiclub.domain.service.board;

import com.comiclub.domain.repository.board.freework.FWBoardRepository;
import com.comiclub.domain.repository.series.SeriesRepository;
import com.comiclub.web.contoller.common.HomeController;
import com.comiclub.web.contoller.board.dto.RealTimeBestDto;
import com.comiclub.web.contoller.board.dto.FreeWorkBoardInfo;
import com.comiclub.web.contoller.board.series.dto.SeriesBoardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BoardScheduler {

    private final SeriesRepository seriesRepository;
    private final FWBoardRepository fwBoardRepository;


    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void fetchDayBestWorks(){
        List<RealTimeBestDto> realTimeBestSeries = seriesRepository.findDayBestSeries().stream()
                .map(series -> new RealTimeBestDto(series))
                .collect(Collectors.toList());
        RealTimeBestDto.REAL_TIME_BEST_SERIES = realTimeBestSeries;

        List<RealTimeBestDto> realTimeBestFreeWork = fwBoardRepository.findDayBestBoards().stream()
                .map(board -> new RealTimeBestDto(board))
                .collect(Collectors.toList());
        RealTimeBestDto.REAL_TIME_BEST_FREE_WORK = realTimeBestFreeWork;

    }

    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
    public void fetchWeekBestWorks(){
        List<SeriesBoardDto> weekBestSeries = seriesRepository.findWeekBestSeries().stream()
                .map(series -> new SeriesBoardDto(series))
                .collect(Collectors.toList());
        HomeController.setWeekBestSeries(weekBestSeries);

        List<FreeWorkBoardInfo> weekBestFreeWork = fwBoardRepository.findWeekBestBoards().stream()
                .map(board -> new FreeWorkBoardInfo(board))
                .collect(Collectors.toList());
        HomeController.setWeekBestFreeWorks(weekBestFreeWork);
    }

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
    public void fetchRankWorks(){
        List<SeriesBoardDto> seriesRanks = seriesRepository.findRankSeries().stream()
                .map(series -> new SeriesBoardDto(series))
                .collect(Collectors.toList());
        HomeController.setSeriesRanks(seriesRanks);

        List<FreeWorkBoardInfo> freeWorkRanks = fwBoardRepository.findRankBoards().stream()
                .map(board -> new FreeWorkBoardInfo(board))
                .collect(Collectors.toList());
        HomeController.setFreeWorkRanks(freeWorkRanks);
    }
}
