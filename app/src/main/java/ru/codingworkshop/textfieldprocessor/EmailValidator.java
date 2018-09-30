package ru.codingworkshop.textfieldprocessor;

import java.util.regex.Pattern;

import ru.codingworkshop.fieldvalidatorlibrary.Validator;

public class EmailValidator implements Validator {
    @Override
    public boolean validate(String textToValidate) {
        return Pattern.compile("\\w+@\\w+\\.\\w+")
                .matcher(textToValidate)
                .find();
    }

    @Override
    public String getErrorText() {
        return "Некорректный формат email";
    }
}
