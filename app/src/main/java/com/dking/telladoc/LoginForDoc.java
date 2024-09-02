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

public class LoginForDoc extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, loginSwitch;
    private TextView registerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_for_doc);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.Dloginemail);
        passwordField = findViewById(R.id.Dloginpass);
        loginButton = findViewById(R.id.DbtnReg);
        registerTextView = findViewById(R.id.DtvRegister);
        loginSwitch = findViewById(R.id.DswitchToLogin);

        loginButton.setOnClickListener(v -> loginUser());

        registerTextView.setOnClickListener(v -> {
            Intent registerIntent = new Intent(LoginForDoc.this, RegisterForDoc.class);
            startActivity(registerIntent);
        });

        loginSwitch.setOnClickListener(v -> {
            Intent registerIntent = new Intent(LoginForDoc.this, Login.class);
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
            Toast.makeText(LoginForDoc.this, "All fields are required", Toast.LENGTH_SHORT).show();
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
                        saveUserRoleToPreferences("Doc");
                        Intent mainIntent = new Intent(LoginForDoc.this, MainDocInterface.class);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        Toast.makeText(LoginForDoc.this, "Authenication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}