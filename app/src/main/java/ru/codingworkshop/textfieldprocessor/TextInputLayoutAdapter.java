package ru.codingworkshop.textfieldprocessor;

import android.support.design.widget.TextInputLayout;

import ru.codingworkshop.fieldvalidatorlibrary.TextFieldViewAdapter;

public class TextInputLayoutAdapter implements TextFieldViewAdapter<TextInputLayout> {
    @Override
    public String getText(TextInputLayout textField) {
        return textField.getEditText().getText().toString();
    }

    @Override
    public void setError(TextInputLayout textField, String error) {
        textField.setError(error);
    }

    public void clearError(TextInputLayout textField) {
        textField.setError("");
        textField.setErrorEnabled(false);
    }
}
