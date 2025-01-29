package ru.practicum.ewm.base.util.IfNotNullIsNotEmpty;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IfNotNullIsNotEmptyConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IfNotNullIsNotEmpty {
    String message() default "If the field does not contain null, then it should not be empty.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
