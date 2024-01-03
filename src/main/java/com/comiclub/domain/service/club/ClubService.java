package com.comiclub.domain.service.club;


import com.comiclub.domain.entity.board.Club;
import com.comiclub.domain.entity.board.FreeBoard;
import com.comiclub.domain.repository.board.free.FreeBoardRepository;
import com.comiclub.domain.repository.club.ClubRepository;
import com.comiclub.web.contoller.club.BoardSearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubService {

    private final ClubRepository clubRepository;
    private final FreeBoardRepository freeBoardRepository;


    public Long saveClub(Club club) {
        Club savedClub = clubRepository.save(club);
        return savedClub.getId();
    }

    public List<Club> findAll() {
        return clubRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Club> findById(Long id) {
        return clubRepository.findById(id);
    }

    public Page<FreeBoard> searchBoard(Long clubId, BoardSearchCond condition, Pageable pageable) {
        return freeBoardRepository.search(clubId, condition, pageable);
    }

    public List<FreeBoard> searchNotice(Long clubId, boolean viewAdult) {
        return freeBoardRepository.searchNotice(clubId, viewAdult);
    }


}
