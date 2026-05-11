package pressing.app.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validateur de numero de telephone via Google libphonenumber.
 * Accepte les numeros vides/null (le champ telephone est optionnel).
 * Quand renseigne, verifie que le numero est un vrai numero international.
 */
public class TelephoneValidator implements ConstraintValidator<TelephoneValide, String> {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        // Champ optionnel : null ou vide = valide
        if (telephone == null || telephone.isBlank()) {
            return true;
        }

        try {
            // Necessite le format international (ex: +33612345678)
            Phonenumber.PhoneNumber number = phoneUtil.parse(telephone, null);
            return phoneUtil.isValidNumber(number);
        } catch (NumberParseException e) {
            return false;
        }
    }
}
