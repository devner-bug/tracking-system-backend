package uk.gov.hmcts.dev.util.helper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
class MessageUtil {
    private final MessageSource messageSource;

    public String message(@NonNull String code){
        return messageSource.getMessage(code, null, null);
    }

    public String message(@NonNull String code, Locale locale){
        return messageSource.getMessage(code, null, locale);
    }
}
