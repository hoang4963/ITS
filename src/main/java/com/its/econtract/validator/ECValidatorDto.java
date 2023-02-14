package com.its.econtract.validator;

import lombok.extern.log4j.Log4j2;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import javax.validation.*;
import java.util.Set;

@Log4j2
@Component
public class ECValidatorDto<T> {

    private Validator validator;

    @PostConstruct
    public void initValidatorFactory() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    public void validate(T instance) {
        Set<ConstraintViolation<T>> violations = validator.validate(instance);
        if (violations.size() > 0) {
            StringBuilder msg = new StringBuilder();
            msg.append("JSON object is not valid. Reasons (").append(violations.size()).append("): ");
            for (ConstraintViolation<T> violation : violations) {
                msg.append(violation.getMessage()).append(", ");
            }
            log.info("msg: {}", msg);
            throw new ConstraintViolationException(msg.toString(), violations);
        }
    }
}
