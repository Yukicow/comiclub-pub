package com.comiclub.web.contoller.board.form;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class WorkBoardSaveForm {

    @NotBlank
    @Length(min = 1, max = 40)
    private String title;

    @Length(max = 100)
    private String authorWord;

    @NotNull
    private boolean adultOnly;

    @NotNull
    private boolean isPublic;

    public String getTitle() {
        return title;
    }

    public String getAuthorWord() {
        return authorWord;
    }

    public boolean getAdultOnly() {
        return adultOnly;
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic){
        this.isPublic = isPublic;
    }
}
