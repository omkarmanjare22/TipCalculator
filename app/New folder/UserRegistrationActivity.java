package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserRegistrationActivity extends Activity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        usernameEditText = findViewById(R.id.registration_username);
        passwordEditText = findViewById(R.id.registration_password);
        registerButton = findViewById(R.id.register_button);
        databaseHelper = new DatabaseHelper(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(UserRegistrationActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                } else {
                    boolean result = databaseHelper.addUser(username, password);
                    if (result) {
                        Toast.makeText(UserRegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        finish(); // Close registration activity after successful registration
                    } else {
                        Toast.makeText(UserRegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
