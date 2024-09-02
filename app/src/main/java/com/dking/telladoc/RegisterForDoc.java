package com.dking.telladoc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dking.telladoc.essentials.EncryptionHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterForDoc extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton;
    private TextView RegisterForDocTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_for_doc);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.DRegemail);
        passwordField = findViewById(R.id.DRegpass);
        loginButton = findViewById(R.id.DbtnReg);

        loginButton.setOnClickListener(v -> loginUser());
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
            Toast.makeText(RegisterForDoc.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        try {
                            EncryptionHelper.storeEncryptedKey(getApplicationContext(), password);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        saveUserRoleToPreferences("Doc");
                        Intent mainIntent = new Intent(RegisterForDoc.this, DoctorBoarding.class);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        Toast.makeText(RegisterForDoc.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}