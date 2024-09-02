// Interface.java
package com.dking.telladoc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dking.telladoc.essentials.EncryptionHelper;

import com.dking.telladoc.essentials.FirestoreUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainDocInterface extends AppCompatActivity {

    private Button btnScanQrCode, btnLogout;
    private LinearLayout patientListLayout;
    private static final String TAG = "QRCodeScannerActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_doc_interface);

        btnScanQrCode = findViewById(R.id.btnScanQrCode);
        btnLogout = findViewById(R.id.btnLogout);
        patientListLayout = findViewById(R.id.patientListLayout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnScanQrCode.setOnClickListener(v -> initiateQrCodeScan());

        fetchScannedPatients();

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainDocInterface.this, Login.class));
            finish();
        });
    }

    private void fetchScannedPatients() {
        String doctorId = mAuth.getCurrentUser().getUid();
        DocumentReference doctorRef = db.collection("doctors").document(doctorId);

        doctorRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, String>> scannedPatients = (List<Map<String, String>>) documentSnapshot.get("scannedPatients");
                if (scannedPatients != null) {
                    for (Map<String, String> patientData : scannedPatients) {
                        String userId = patientData.get("userId");
                        String hash = patientData.get("hash");
                        byte[] decodedKey = Base64.getDecoder().decode(hash);
                        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                        fetchUserDataNormie(userId, originalKey, hash);
                    }
                }
            } else {
                Toast.makeText(this, "No scanned patients found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching scaned patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("MainDocInterface", "Error fetching scanned patiens", e);
        });
    }

    private void initiateQrCodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        integrator.setPrompt("Scan QR Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        String userId = ((result.getContents()).split("\\|#@>"))[0];
        String passW = ((result.getContents()).split("\\|#@>"))[1];
        Log.d(TAG, "VALUES "+userId+"  "+ passW);
        byte[] decodedKey = Base64.getDecoder().decode(passW);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        Log.d(TAG, "SCANNED DATA " + result.getContents() + " " + originalKey);
        if (result != null && result.getContents() != null) {
            fetchUserData(userId, originalKey, passW);
        } else {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserDataNormie(String userId, SecretKey passWord, String passWW) {
        FirestoreUtils.fetchEncryptedUserData(userId, passWord, getApplicationContext(), new FirestoreUtils.FirestoreCallback() {
            @Override
            public void onCallback(Map<String, Object> documentSnapshot) {
                if (documentSnapshot != null && !documentSnapshot.isEmpty()) {
                    Log.d(TAG, "Decrypted Data: " + documentSnapshot);

                    String name = (String) documentSnapshot.get("name");
                    if (name != null) {
                        Log.d(TAG, "Patient Name: " + name);
                    } else {
                        Log.e(TAG, "Name field is missing in the decrypted dat");
                    }

                    List<Map<String, String>> records = (List<Map<String, String>>) documentSnapshot.get("Records");
                    if (records != null) {
                        Log.d(TAG, "Records: " + records);
                        addPatientView(userId, documentSnapshot);
//                        saveScannedPatientData(userId, passWW);

//                        for (Map<String, String> record : records) {
//                            String diseaseName = record.get("diseaseName");
//                            String since = record.get("since");
//                            String type = record.get("type");
//                            String description = record.get("description");
//
//                            if (diseaseName == null || since == null || type == null || description == null) {
//                                Log.e(TAG, "Incomplete record found: " + record);
//                                continue;  // Skip incomplete records
//                            }
//                        }
                    } else {
                        Log.e(TAG, "Records field is missing in the data");
                        Toast.makeText(getApplicationContext(), "No records found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Document is empty or null.");
                    Toast.makeText(getApplicationContext(), "No records found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchUserData(String userId, SecretKey passWord, String passWW) {
        FirestoreUtils.fetchEncryptedUserData(userId, passWord, getApplicationContext(), new FirestoreUtils.FirestoreCallback() {
            @Override
            public void onCallback(Map<String, Object> documentSnapshot) {
                if (documentSnapshot != null && !documentSnapshot.isEmpty()) {
                    Log.d(TAG, "Decrypted Data: " + documentSnapshot);

                    String name = (String) documentSnapshot.get("name");
                    if (name != null) {
                        Log.d(TAG, "Patient Name: " + name);
                    } else {
                        Log.e(TAG, "Name field is missing in data");
                    }

                    List<Map<String, String>> records = (List<Map<String, String>>) documentSnapshot.get("Records");
                    if (records != null) {
                        Log.d(TAG, "Records: " + records);
                        addPatientView(userId, documentSnapshot);
                        saveScannedPatientData(userId, passWW);

//                        for (Map<String, String> record : records) {
//                            String diseaseName = record.get("diseaseName");
//                            String since = record.get("since");
//                            String type = record.get("type");
//                            String description = record.get("description");
//
//                            if (diseaseName == null || since == null || type == null || description == null) {
//                                Log.e(TAG, "Incomplete record found: " + record);
//                                continue;  // Skip incomplete records
//                            }
//                        }
                    } else {
                        Log.e(TAG, "Records field is missing in");
                        Toast.makeText(getApplicationContext(), "No records found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Document is empty");
                    Toast.makeText(getApplicationContext(), "No records found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveScannedPatientData(String userId, String hash) {
        String doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the doctor's document
        DocumentReference doctorRef = db.collection("doctors").document(doctorId);

        // Create a map for the scanned patient data
        Map<String, String> scannedPatient = new HashMap<>();
        scannedPatient.put("userId", userId);
        scannedPatient.put("hash", hash);

        // Add the scanned patient data to the 'scannedPatients' array in Firestore
        doctorRef.update("scannedPatients", FieldValue.arrayUnion(scannedPatient))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Patient data saved successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, update the UI to show the scanned patient data
//                    updateUIWithScannedPatientData(userId);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG,"FAILED: "+ e.getMessage());
                    Toast.makeText(this, "Failed to save patient data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void addPatientView(String userId, Map<String, Object> patientData) {
        View patientView = getLayoutInflater().inflate(R.layout.item_patient, null);
        TextView tvPatientName = patientView.findViewById(R.id.tvPatientName);
        TextView tvPatientDetails = patientView.findViewById(R.id.tvPatientDetails);
        Button btnExpand = patientView.findViewById(R.id.btnExpand);
        String name = (String) patientData.get("name");
        String age = (String) patientData.get("age");
        tvPatientName.setText(name);

        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("Age: ").append(age).append("\n");
        if (patientData.containsKey("Records")) {
            List<Map<String, String>> diseases = (List<Map<String, String>>) patientData.get("Records");
            for (Map<String, String> disease : diseases) {
                detailsBuilder.append(disease.get("type")).append(": ").append(disease.get("diseaseName"))
                        .append(", Since: ").append(disease.get("since"))
                        .append(", Description: ").append(disease.get("description")).append("\n");
            }
        }

        tvPatientDetails.setText(detailsBuilder.toString());
        tvPatientDetails.setVisibility(View.GONE);

        btnExpand.setOnClickListener(v -> {
            if (tvPatientDetails.getVisibility() == View.GONE) {
                tvPatientDetails.setVisibility(View.VISIBLE);
                btnExpand.setText("Collapse");
            } else {
                tvPatientDetails.setVisibility(View.GONE);
                btnExpand.setText("Expand");
            }
        });

        patientListLayout.addView(patientView);
    }
}