package ru.codingworkshop.fieldvalidatorlibrary;

public interface Validator {
    boolean validate(String textToValidate);
    String getErrorText();
}
