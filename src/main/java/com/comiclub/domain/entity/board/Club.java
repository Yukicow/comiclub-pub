package com.comiclub.domain.entity.board;

import com.comiclub.domain.entity.BaseDateEntity;
import com.comiclub.domain.entity.board.enumtype.Genre;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Table(name = "club",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "genre_club_name_unique",
                columnNames = {"genre", "clubName"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Club extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "club_id")
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(nullable = false, length = 15)
    private String clubName;

    public Club(Long id) {
        this.id = id;
    }

    public Club(Genre genre, String clubName) {
        this.genre = genre;
        this.clubName = clubName;
    }
}
