package com.comiclub.web.contoller.club.dto;

import com.comiclub.domain.entity.board.Club;
import com.comiclub.domain.entity.board.enumtype.Genre;
import lombok.Data;

@Data
public class ClubDto {

    private Long id;
    private Genre genre;
    private String clubName;

    public ClubDto(Club club) {
        this.id = club.getId();
        this.genre = club.getGenre();
        this.clubName = club.getClubName();
    }
}
