package hu.traileddevice.flashcard.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.lang.annotation.*;

@Email(message = "Please provide a valid Google mail address")
@Pattern(regexp = "^[^@]+@(gmail|googlemail|google).com$", message = "Please provide a valid Google mail address")
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GmailValidator {
    String message() default "Please provide a valid Google mail address";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
