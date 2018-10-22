package ru.codingworkshop.textfieldprocessor.generated;

import java.util.Arrays;
import java.util.List;

import ru.codingworkshop.fieldvalidatorlibrary.TextFieldViewAdapter;
import ru.codingworkshop.fieldvalidatorlibrary.Validator;
import ru.codingworkshop.textfieldprocessor.EmailValidator;
import ru.codingworkshop.textfieldprocessor.EmptyFieldValidator;
import ru.codingworkshop.textfieldprocessor.MainActivity;
import ru.codingworkshop.textfieldprocessor.TextInputLayoutAdapter;

public class MainActivityValidator {
    private MainActivity container;

    private MainActivityValidator(MainActivity mainActivity) {
        this.container = mainActivity;
    }

    public static MainActivityValidator init(MainActivity mainActivity) {
        return new MainActivityValidator(mainActivity);
    }

    public boolean validateEmail() {
        List<Validator> validators = Arrays.asList(
                new EmptyFieldValidator(),
                new EmailValidator()
        );

        return validateField(validators, container.email, new TextInputLayoutAdapter());
    }

    private static <T> boolean validateField(Iterable<Validator> validators, T field, TextFieldViewAdapter<T> adapter) {
        String text = adapter.getText(field);
        for (Validator v : validators) {
            if (v.validate(text)) {
                adapter.clearError(field);
            } else {
                adapter.setError(field, v.getErrorText());
                return false;
            }
        }
        return true;
    }

    public void validateAll() {
        validateEmail();
    }
}
