package com.vitaclinica.agendamento.validation;

import jakarta.validation.*;

public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext ctx) {
        if (cpf == null || cpf.isBlank()) return false;
        String numeros = cpf.replaceAll("[^0-9]", "");
        if (numeros.length() != 11) return false;
        // Rejeita sequências repetidas (111.111.111-11, etc.)
        if (numeros.chars().distinct().count() == 1) return false;
        return validarDigitos(numeros);
    }

    private boolean validarDigitos(String cpf) {
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        int digito1 = (soma * 10) % 11;
        if (digito1 == 10) digito1 = 0;
        if (digito1 != Character.getNumericValue(cpf.charAt(9))) return false;

        soma = 0;
        for (int i = 0; i < 10; i++) soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        int digito2 = (soma * 10) % 11;
        if (digito2 == 10) digito2 = 0;
        return digito2 == Character.getNumericValue(cpf.charAt(10));
    }
}
