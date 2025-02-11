package org.ducanh.apiiam.helpers;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class ValidationHelpers {
    public static void valArg(Supplier<Boolean> validateFunction, Supplier<RuntimeException> runtimeExceptionSupplier) {
        Boolean valid = validateFunction.get();
        valArg(valid, runtimeExceptionSupplier);
    }

    public static void valArg(Boolean valid, Supplier<RuntimeException> runtimeExceptionSupplier) {
        if (!valid) {
            throw runtimeExceptionSupplier.get();
        }
    }

    public static boolean stringContainSpecialCharacters(String password) {
        String specialCharPattern = ".*[!@#$%^&*()\\-+=<>?{}\\[\\]~].*";
        return password.matches(specialCharPattern);
    }
}
