package com.atguigu.common.valid;

import jakarta.validation.ConstraintValidator;

import java.util.HashSet;
import java.util.Set;

public class ListValueConstraintValidator implements ConstraintValidator<ListValues, Integer> {

    private Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(ListValues constraintAnnotation) {
        int[] values = constraintAnnotation.values();
        for (int v : values) {
            set.add(v);
        }
    }

    /*
    @param value The value to validate
    @param context The context in which the constraint is evaluated
     */
    @Override
    public boolean isValid(Integer value, jakarta.validation.ConstraintValidatorContext context) {

        return set.contains(value);
    }
}
