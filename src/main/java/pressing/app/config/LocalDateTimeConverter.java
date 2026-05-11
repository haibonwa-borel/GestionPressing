package pressing.app.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Convertisseur pour gerer les dates LocalDateTime avec SQLite.
 * Stocke la date en tant que String au format "yyyy-MM-dd HH:mm:ss".
 * Peut lire plusieurs formats pour supporter les anciennes donnees.
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter WRITE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Formats pouvant etre presents dans la base (anciens et nouveaux)
    private static final DateTimeFormatter[] READ_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,                        // 2026-05-07T10:00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS")
    };

    @Override
    public String convertToDatabaseColumn(LocalDateTime locDateTime) {
        return (locDateTime == null ? null : locDateTime.format(WRITE_FORMAT));
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String sqlTimestamp) {
        if (sqlTimestamp == null || sqlTimestamp.isBlank()) return null;

        for (DateTimeFormatter fmt : READ_FORMATS) {
            try {
                return LocalDateTime.parse(sqlTimestamp, fmt);
            } catch (DateTimeParseException ignored) {
                // Essayer le format suivant
            }
        }
        throw new RuntimeException("Format de date non reconnu dans SQLite : " + sqlTimestamp);
    }
}
