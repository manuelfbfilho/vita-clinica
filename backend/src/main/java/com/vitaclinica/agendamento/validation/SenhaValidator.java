package com.vitaclinica.agendamento.validation;

import jakarta.validation.*;

public class SenhaValidator implements ConstraintValidator<ValidSenha, String> {

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext ctx) {
        if (senha == null) return false;
        // Mínimo 10 chars | 1 maiúscula | 1 minúscula | 1 número | 1 especial
        return senha.length() >= 10
            && senha.chars().anyMatch(Character::isUpperCase)
            && senha.chars().anyMatch(Character::isLowerCase)
            && senha.chars().anyMatch(Character::isDigit)
            && senha.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);
    }
}
