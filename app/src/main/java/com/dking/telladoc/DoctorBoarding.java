package com.dking.telladoc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dking.telladoc.essentials.FirestoreUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;

public class DoctorBoarding extends AppCompatActivity {

    private static final String TAG = "BoardingActivity";

    private EditText editTextName, editTextAge;
    private Spinner spinnerBloodgroup;
    private Button buttonSubmit;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_boarding);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextName = findViewById(R.id.DboardingName);
        editTextAge = findViewById(R.id.DboardingNumber);
        spinnerBloodgroup = findViewById(R.id.Dbloodspinner);
        buttonSubmit = findViewById(R.id.DboardinSubmit);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.blood_groups, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodgroup.setAdapter(adapter);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }

        });
    }

    private void saveUserData() {
        String name = editTextName.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();
        String bloodGroup = spinnerBloodgroup.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(bloodGroup)) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("age", age);
        userData.put("bloodGroup", bloodGroup);

        try {
            FirestoreUtils.saveEncryptedDocData(userId, userData, getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent mainIntent = new Intent(DoctorBoarding.this, MainDocInterface.class);
        startActivity(mainIntent);

        // Save the user data to Firestore
//        db.collection("users").document(userId)
//                .set(userData)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(BoardingActivity.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, "User data successfully written!");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(BoardingActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        Log.w(TAG, "Error writing document", e);
//                    }
//                });
    }
}