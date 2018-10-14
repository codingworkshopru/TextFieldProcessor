package ru.codingworkshop.textfieldprocessor;

import ru.codingworkshop.fieldvalidatorlibrary.Validator;

class PasswordValidator implements Validator {
    @Override
    public boolean validate(String textToValidate) {
        return textToValidate != null && textToValidate.length() > 5;
    }

    @Override
    public String getErrorText() {
        return "Пароль должен быть больше 5 символов";
    }
}
