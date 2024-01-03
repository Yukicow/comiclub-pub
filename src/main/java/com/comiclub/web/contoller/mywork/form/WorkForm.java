package com.comiclub.web.contoller.mywork.form;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class WorkForm {

    @Length(min = 1, max = 40)
    private String title;

    @NotNull
    private Boolean autoMode;

    @NotNull
    private Boolean freeUse;
}
