package com.vitaclinica.agendamento.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CpfValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCpf {
    String message() default "CPF inválido. Informe no formato 000.000.000-00";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
