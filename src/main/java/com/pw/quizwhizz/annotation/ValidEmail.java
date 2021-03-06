package com.pw.quizwhizz.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Adnotacja @ValidEmail powiazana z ValidEmailValidator
 * @author Michał Nowiński
 */
@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidEmailValidator.class)
@Documented
public @interface ValidEmail {
    String message() default "{org.hibernate.validator.constraints.Email.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}