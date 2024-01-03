package com.comiclub.web.util.validation.validator;

import com.comiclub.web.util.validation.annotation.NoSpace;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpaceValidator implements ConstraintValidator<NoSpace, String> {

    @Override
    public void initialize(NoSpace constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !value.contains(" ");
    }
}
