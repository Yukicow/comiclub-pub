package com.comiclub.domain.service.admin;

import com.comiclub.domain.entity.board.FreeBoard;
import com.comiclub.domain.repository.board.free.FreeBoardRepository;
import com.comiclub.web.contoller.club.BoardSearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FreeBoardRepository freeBoardRepository;

    public Page<FreeBoard> searchFreeBoard(Long clubId, BoardSearchCond boardSearchCond, Pageable pageable) {
        return freeBoardRepository.search(clubId, boardSearchCond, pageable);
    }

}
