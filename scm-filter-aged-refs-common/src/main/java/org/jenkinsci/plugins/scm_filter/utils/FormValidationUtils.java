package org.jenkinsci.plugins.scm_filter.utils;

import hudson.util.FormValidation;

public class FormValidationUtils {
    public static FormValidation checkRetentionDays(String value) {
        FormValidation formValidation = FormValidation.ok();

        try {
            if (value == null || value.isBlank()) {
                formValidation = FormValidation.error("Not a number");
            } else {
                int val = Integer.parseInt(value);
                if (val < 1) {
                    formValidation = FormValidation.error("Not a positive number");
                }
            }
        } catch (NumberFormatException e) {
            formValidation = FormValidation.error("Not a number");
        }

        return formValidation;
    }
}
