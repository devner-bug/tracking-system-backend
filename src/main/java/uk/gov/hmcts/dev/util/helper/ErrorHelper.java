package uk.gov.hmcts.dev.util.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ErrorHelper {
    private final MessageUtil messageUtil;

    public String duplicateTitleError(){
        return messageUtil.message("error.duplicate.title");
    }

    public String duplicateEntityError(){
        return messageUtil.message("error.duplicate.entity");
    }

    public String caseNotFoundError() {
        return messageUtil.message("error.case.not.found");
    }

    public String fieldValidationFailedError(){
        return messageUtil.message("error.field.validation.failed");
    }

    public String generalError(){
        return messageUtil.message("error.general.issue");
    }

    public String unexpectedError(){
        return messageUtil.message("error.unexpected");
    }
}
