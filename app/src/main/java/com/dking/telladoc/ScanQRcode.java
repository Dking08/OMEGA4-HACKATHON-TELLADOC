package com.dking.telladoc;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dking.telladoc.R;
import com.dking.telladoc.essentials.Disease;
import com.dking.telladoc.essentials.DiseaseAdapter;
import com.dking.telladoc.essentials.FirestoreUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ScanQRcode extends AppCompatActivity {

    private static final String TAG = "QRCodeScannerActivity";
    private FirebaseFirestore db;
    private DiseaseAdapter diseaseAdapter;
    private List<Disease> diseaseList;
    private RecyclerView recyclerViewDiseases;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        db = FirebaseFirestore.getInstance();
        diseaseList = new ArrayList<>();
        diseaseAdapter = new DiseaseAdapter(diseaseList);
        recyclerViewDiseases = findViewById(R.id.ScanQRViewDiseases);
        recyclerViewDiseases.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDiseases.setAdapter(diseaseAdapter);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String userId = ((result.getContents()).split("\\|#@>"))[0];
                String passW = ((result.getContents()).split("\\|#@>"))[1];
                Log.d(TAG, "VALUES "+userId+"  "+ passW);
                byte[] decodedKey = Base64.getDecoder().decode(passW);
                SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                Log.d(TAG, "SCANNED DATA " + result.getContents() + " " + originalKey);
                fetchUserData(userId, originalKey);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void fetchUserData(String userId, SecretKey passWord) {
        FirestoreUtils.fetchEncryptedUserData(userId, passWord, getApplicationContext(), new FirestoreUtils.FirestoreCallback() {
            @Override
            public void onCallback(Map<String, Object> documentSnapshot) {
                if (documentSnapshot != null && !documentSnapshot.isEmpty()) {
                    Log.d(TAG, "ONCALLBACK" + documentSnapshot);
                    String name = (String) documentSnapshot.get("name");
                    List<Map<String, String>> records = (List<Map<String, String>>) documentSnapshot.get("Records");
                    Log.d(TAG, "RECORDS" + records);
                    diseaseList.clear();

                    if (records != null) {
                        for (Map<String, String> record : records) {
                            String diseaseName = record.get("diseaseName");
                            String since = record.get("since");
                            String type = record.get("type");
                            String description = record.get("description");
                            Disease disease = new Disease(diseaseName, since, type, description);
                            Log.d(TAG, "VALUES" + disease);
                            diseaseList.add(disease);
                        }
                    }

                    diseaseAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "No records found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
