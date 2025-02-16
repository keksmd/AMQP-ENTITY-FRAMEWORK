package dada.tuda.framework.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class ExpiredDateTimeValidator implements ConstraintValidator<NotExpired, LocalDateTime> {

    public ExpiredDateTimeValidator() {

    }

    @Override
    public void initialize(NotExpired constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.isBefore(LocalDateTime.now());
    }
}
