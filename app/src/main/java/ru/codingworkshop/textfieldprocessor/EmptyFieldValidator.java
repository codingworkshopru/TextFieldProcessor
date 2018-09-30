package ru.codingworkshop.textfieldprocessor;

import ru.codingworkshop.fieldvalidatorlibrary.Validator;

public class EmptyFieldValidator implements Validator {
    @Override
    public boolean validate(String textToValidate) {
        return textToValidate != null && !textToValidate.isEmpty();
    }

    @Override
    public String getErrorText() {
        return "Поле не может быть пустым";
    }
}
