package com.vitaclinica.agendamento.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CrmValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCrm {
    String message() default "CRM inválido. Formato esperado: CRM/UF-NNNNN";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
