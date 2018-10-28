package ru.codingworkshop.textfieldprocessor.generated;

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
        return validateField(container.email, new TextInputLayoutAdapter(),
                new EmptyFieldValidator(), new EmailValidator());
    }

    private static <T> boolean validateField(T field, TextFieldViewAdapter<T> adapter, Validator... validators) {
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

    public boolean validateAll() {
        return validateEmail();
    }
}
