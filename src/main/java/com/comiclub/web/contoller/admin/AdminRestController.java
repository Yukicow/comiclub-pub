package com.comiclub.web.contoller.admin;

import com.comiclub.domain.entity.board.FreeBoard;
import com.comiclub.domain.entity.board.FreeWorkBoard;
import com.comiclub.domain.entity.sereis.Series;
import com.comiclub.domain.repository.board.freework.FWBoardRepository;
import com.comiclub.domain.repository.series.SeriesRepository;
import com.comiclub.domain.service.board.FWBoardService;
import com.comiclub.domain.service.board.FreeBoardService;
import com.comiclub.domain.service.board.SeriesService;
import com.comiclub.web.exception.NotFoundException;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/")
public class AdminRestController {

    private final FreeBoardService freeBoardService;
    private final SeriesService seriesService;
    private final FWBoardService fwBoardService;

    private final SeriesRepository seriesRepository;
    private final FWBoardRepository fwBoardRepository;

    /**
     * FreeBoard
     * */
    @DeleteMapping("board/free/{boardId}")
    public ResponseEntity<Object> deleteFreeBoard(@PathVariable Long boardId){

        freeBoardService.deleteBoard(new FreeBoard(boardId));
        return ResponseEntity.ok().build();
    }


    /**
     * Series
     * */
    @PatchMapping("work/series/{seriesId}")
    public ResponseEntity<Object> closeSeries(@PathVariable Long seriesId){

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("There is no series id with '" + seriesId + "'"));

        seriesService.closePub(series);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("work/series/{seriesId}")
    public ResponseEntity<Object> deleteSeries(@PathVariable Long seriesId){

        seriesService.deleteSeries(new Series(seriesId));
        return ResponseEntity.ok().build();
    }




    /**
     * FreeWork Board
     * */
    @PatchMapping("work/free/{boardId}")
    public ResponseEntity<Object> closeFreeWorkBoard(@PathVariable Long boardId){

        FreeWorkBoard board = fwBoardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no FreeWorkBoard id with '" + boardId + "'"));

        fwBoardService.changePub(board);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("work/free/{boardId}")
    public ResponseEntity<Object> deleteFreeWorkBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long boardId){

        fwBoardService.deleteWorkBoard(boardId, memberInfo.getId());
        return ResponseEntity.ok().build();
    }
}
