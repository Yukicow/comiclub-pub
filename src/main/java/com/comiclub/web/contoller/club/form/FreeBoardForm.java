package com.comiclub.web.contoller.club.form;


import com.comiclub.domain.entity.board.FreeBoard;
import com.comiclub.domain.entity.board.enumtype.Topic;
import com.comiclub.web.util.validation.annotation.NoSpace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter @Setter
@NoArgsConstructor
public class FreeBoardForm {

    @NotNull
    private Topic topic;

    private Boolean adultOnly;

    @Length(min = 2, max = 15)
    @NoSpace
    private String writer;

    @Length(min = 4, max = 16)
    @NoSpace
    private String password;

    @NotBlank
    @Length(min = 1, max = 40)
    private String title;

    @Length(max = 10000)
    private String content;


    public FreeBoardForm(FreeBoard freeBoard) {
        this.topic = freeBoard.getTopic();
        this.adultOnly = freeBoard.getAdultOnly();
        this.writer = freeBoard.getWriter();
        this.password = freeBoard.getPassword();
        this.title = freeBoard.getTitle();
        this.content = freeBoard.getContent();
    }
}
