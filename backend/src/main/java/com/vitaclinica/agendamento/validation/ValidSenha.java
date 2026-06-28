package com.vitaclinica.agendamento.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SenhaValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSenha {
    String message() default "Senha deve ter mínimo 10 caracteres com letra maiúscula, minúscula, número e caractere especial";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
