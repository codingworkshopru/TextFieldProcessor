package ru.codingworkshop.textfieldprocessor;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;

import ru.codingworkshop.fieldvalidatorlibrary.TextFieldValidator;
import ru.codingworkshop.textfieldprocessor.generated.MainActivityValidator;

public class MainActivity extends AppCompatActivity {
    @TextFieldValidator(
            validator = {
                    EmptyFieldValidator.class,
                    EmailValidator.class
            },
            adapter = TextInputLayoutAdapter.class
    )
    public TextInputLayout email;

    @TextFieldValidator(
            validator = PasswordValidator.class,
            adapter = TextInputLayoutAdapter.class
    )
    public TextInputLayout password;

    private MainActivityValidator bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.textInputLayout);
        password = findViewById(R.id.textInputLayout2);
        bind = MainActivityValidator.init(this);
        email.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (bind != null) {
                    bind.validateEmail();
                }
            }
        });
        findViewById(R.id.button).setOnClickListener(unused -> {
            if (bind != null) {
                bind.validateAll();
            }
        });
    }
}
