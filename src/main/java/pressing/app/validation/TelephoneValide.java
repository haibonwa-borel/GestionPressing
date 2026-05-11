package pressing.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation de validation pour numero de telephone international.
 * Utilise Google libphonenumber pour verifier que le numero est reel et valide.
 * Exemple valide : +33612345678, +2376xxxxxxxx
 */
@Documented
@Constraint(validatedBy = TelephoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TelephoneValide {
    String message() default "Le numero de telephone est invalide. Utilisez le format international ex: +33612345678";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
