package com.comiclub.domain.repository.board.free;

import com.comiclub.domain.entity.board.FreeBoard;
import com.comiclub.web.contoller.club.BoardSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JpaFreeBoardRepository {

    public Page<FreeBoard> search(Long clubId, BoardSearchCond condition, Pageable pageable);

    public List<FreeBoard> searchNotice(Long clubId, boolean viewAdult);
}
