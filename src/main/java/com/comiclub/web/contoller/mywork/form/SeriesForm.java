package com.comiclub.web.contoller.mywork.form;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SeriesForm {

    @NotBlank
    @Length(min = 1, max = 40)
    private String name;

    @Length(max = 255)
    private String description;

    @Length(max = 110)
    private String tag;

    @NotNull
    private Boolean adultOnly;

    @NotNull
    private Boolean pub;

}
