package ru.codingworkshop.textfieldprocessor.generated;

import android.support.design.widget.TextInputLayout;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ru.codingworkshop.fieldvalidatorlibrary.Validator;
import ru.codingworkshop.textfieldprocessor.EmailValidator;
import ru.codingworkshop.textfieldprocessor.EmptyFieldValidator;
import ru.codingworkshop.textfieldprocessor.MainActivity;
import ru.codingworkshop.textfieldprocessor.TextInputLayoutAdapter;

public class MainActivityValidator {
    private Object container;
    private Map<String, Field> fields;

    private MainActivityValidator() {
        fields = new HashMap<>();
    }

    public static MainActivityValidator init(MainActivity mainActivity) {
        MainActivityValidator mainActivityValidator = new MainActivityValidator();
        mainActivityValidator.container = mainActivity;
        try {
            String fieldName = "email";
            Field email = MainActivity.class.getField(fieldName);
            email.setAccessible(true);
            mainActivityValidator.fields.put(fieldName, email);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return mainActivityValidator;
    }

    public void validateEmail() {
        Field email = fields.get("email");
        TextInputLayout o = null;
        try {
            o = (TextInputLayout) email.get(container);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        TextInputLayoutAdapter adapter = new TextInputLayoutAdapter();
        String text = adapter.getText(o);

        Validator[] validators = {
                new EmptyFieldValidator(),
                new EmailValidator()
        };

        for (Validator v : validators) {
            if (v.validate(text)) {
                adapter.clearError(o);
            } else {
                adapter.setError(o, v.getErrorText());
                break;
            }
        }
    }

    public void validateAll() {
        validateEmail();
    }
}
