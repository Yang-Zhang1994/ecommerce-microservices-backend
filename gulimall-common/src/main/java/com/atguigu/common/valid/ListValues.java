package com.atguigu.common.valid;

import jakarta.validation.Constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {ListValueConstraintValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER, TYPE_USE, CONSTRUCTOR})
@Retention(RUNTIME)
public @interface ListValues {
    int[] values() default {};
    String message() default "{com.atguigu.common.valid.ListValues.message}";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}
