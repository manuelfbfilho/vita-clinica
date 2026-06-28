package com.vitaclinica.agendamento.validation;

import jakarta.validation.*;

public class CrmValidator implements ConstraintValidator<ValidCrm, String> {

    // Formato: CRM/UF-NNNNN (ex: CRM/PE-12345)
    private static final String CRM_REGEX = "^CRM/[A-Z]{2}-\\d{4,6}$";

    @Override
    public boolean isValid(String crm, ConstraintValidatorContext ctx) {
        if (crm == null || crm.isBlank()) return false;
        return crm.trim().matches(CRM_REGEX);
    }
}
