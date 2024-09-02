package com.dking.telladoc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dking.telladoc.essentials.EncryptionHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, DocButton;
    private TextView registerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.loginemail);
        passwordField = findViewById(R.id.loginpass);
        loginButton = findViewById(R.id.btnReg);
        registerTextView = findViewById(R.id.tvRegister);
        DocButton = findViewById(R.id.SwtichtoDlogin);

        loginButton.setOnClickListener(v -> loginUser());

        registerTextView.setOnClickListener(v -> {
            Intent registerIntent = new Intent(Login.this, Register.class);
            startActivity(registerIntent);
        });

        DocButton.setOnClickListener(v -> {
            Intent registerIntent = new Intent(Login.this, LoginForDoc.class);
            startActivity(registerIntent);
        });

    }

    private void saveUserRoleToPreferences(String role) {
        SharedPreferences sharedPreferences = getSharedPreferences("TellADocPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userRole", role);
        editor.apply();
    }

    private void loginUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(Login.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        try {
                            EncryptionHelper.storeEncryptedKey(getApplicationContext(), password);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        saveUserRoleToPreferences("pat");
                        Intent mainIntent = new Intent(Login.this, Interface.class);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}