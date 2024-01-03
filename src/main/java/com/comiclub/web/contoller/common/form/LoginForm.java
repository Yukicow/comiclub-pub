package com.comiclub.web.contoller.common.form;


import com.comiclub.web.util.validation.annotation.NoSpace;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter @Setter
public class LoginForm {


    @Length(min = 6, max = 30)
    @NoSpace
    private String loginId;

    @Length(min = 8, max = 16)
    @NoSpace
    private String password;

}
