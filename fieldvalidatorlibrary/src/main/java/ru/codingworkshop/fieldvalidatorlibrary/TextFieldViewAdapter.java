package ru.codingworkshop.fieldvalidatorlibrary;

public interface TextFieldViewAdapter<T> {
    String getText(T textField);
    void setError(T textField, String error);
}
