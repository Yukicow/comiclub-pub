package com.comiclub.domain.repository.board.freework;

import com.comiclub.domain.entity.board.FreeWorkBoard;
import com.comiclub.web.contoller.board.freework.dto.FreeWorkBoardSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface JpaFWBoardRepository {


    Page<FreeWorkBoard> searchFreeWorkBoard(FreeWorkBoardSearchCond cond, Pageable pageable);

    List<FreeWorkBoard> findWritersFreeWorkBoards(Long memberId, boolean adultOnly, Pageable pageable);

    Optional<FreeWorkBoard> findOneByWorkId(Long workId);
    List<FreeWorkBoard> findDayBestBoards();

    List<FreeWorkBoard> findWeekBestBoards();

    List<FreeWorkBoard> findRankBoards();

    Page<FreeWorkBoard> adminSearchFreeWorkBoard(FreeWorkBoardSearchCond cond, Pageable pageable);
}
