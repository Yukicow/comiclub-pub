package com.comiclub.web.contoller.board.dto;


import com.comiclub.domain.entity.board.FreeWorkBoard;
import com.comiclub.domain.entity.sereis.Series;
import lombok.Data;

import java.util.List;

@Data
public class RealTimeBestDto {

    public static List<RealTimeBestDto> REAL_TIME_BEST_FREE_WORK;
    public static List<RealTimeBestDto> REAL_TIME_BEST_SERIES;

    private Long id;
    private String title;

    public RealTimeBestDto(FreeWorkBoard board) {
        this.id = board.getId();
        this.title = board.getTitle();
    }

    public RealTimeBestDto(Series series) {
        this.id = series.getId();
        this.title = series.getName();
    }
}
