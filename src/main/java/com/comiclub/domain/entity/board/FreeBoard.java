package com.comiclub.domain.entity.board;

import com.comiclub.domain.entity.BaseDateEntity;
import com.comiclub.domain.entity.member.Member;
import com.comiclub.domain.entity.board.enumtype.Topic;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "free_board")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeBoard extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "free_board_id")
    private Long id;

    private Long memberId;

    @Column(nullable = false)
    private Long clubId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Topic topic;

    @Column(nullable = false, length = 15)
    private String writer;

    @Column(length = 16)
    private String password;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 10000)
    private String content;

    @ColumnDefault("false")
    @Column(nullable = false)
    private Boolean adultOnly;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Integer totalComment;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Long totalView;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Integer totalLike;


    public FreeBoard(Long id) {
        this.id = id;
    }

    private FreeBoard(Long memberId, Long clubId, Topic topic, String writer, String password, String title,
                     String content, Boolean adultOnly, Integer totalComment, Long totalView, Integer totalLike) {
        this.memberId = memberId;
        this.clubId = clubId;
        this.topic = topic;
        this.writer = writer;
        this.password = password;
        this.title = title;
        this.content = content;
        this.adultOnly = adultOnly;
        this.totalComment = totalComment;
        this.totalView = totalView;
        this.totalLike = totalLike;
    }

    public static FreeBoard createNewFreeBoard(Long memberId, Long clubId, Topic topic, String writer, String password, String title,
                                        String content, Boolean adultOnly){
        return new FreeBoard(
                memberId,
                clubId,
                topic,
                writer,
                password,
                title,
                content,
                adultOnly,
                0,0L,0
        );
    }

    public void changeTopic(Topic topic){
        this.topic = topic;
    }
    public void changeWriter(String writer){
        this.writer = writer;
    }
    public void changePassword(String password){
        this.password = password;
    }
    public void changeTitle(String title){
        this.title = title;
    }
    public void changeContent(String content){
        this.content = content;
    }
    public void changeAdultOnly(boolean adultOnly){
        this.adultOnly = adultOnly;
    }


}
