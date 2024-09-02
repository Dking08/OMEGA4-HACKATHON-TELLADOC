package com.dking.telladoc;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dking.telladoc.R;
import com.dking.telladoc.essentials.Disease;
import com.dking.telladoc.essentials.DiseaseAdapter;
import com.dking.telladoc.essentials.EncryptionHelper;
import com.dking.telladoc.essentials.FirestoreUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private EditText etDisease, etSince, etDescription;
    private RadioButton RadA1,RadD1;
    private TextView nameOfkid;
    private Button btnSave, btnAddDisease;
    private RecyclerView recyclerViewDiseases;
    private DiseaseAdapter diseaseAdapter;
    private List<Disease> diseaseList;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
//        etName = view.findViewById(R.id.etName);
        nameOfkid = view.findViewById(R.id.NameofBoi);
//        etAge = view.findViewById(R.id.etAge);
        etDisease = view.findViewById(R.id.etDisease);
        etDescription = view.findViewById(R.id.etDesc);
        RadA1 = view.findViewById(R.id.AllergyToggle);
        RadD1 = view.findViewById(R.id.DiseaseToggle);
        etSince = view.findViewById(R.id.etSince);
//        btnSave = view.findViewById(R.id.btnSave);
        btnAddDisease = view.findViewById(R.id.btnAddDisease);
        recyclerViewDiseases = view.findViewById(R.id.recyclerViewDiseases);

        diseaseList = new ArrayList<>();
        diseaseAdapter = new DiseaseAdapter(diseaseList);
        recyclerViewDiseases.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDiseases.setAdapter(diseaseAdapter);

        btnAddDisease.setOnClickListener(v -> {addDiseaseRecord();
            try {
                saveMedicalRecords();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }});



        fetchMedicalRecords();

        return view;
    }

    private void addDiseaseRecord() {

        String diseaseName = etDisease.getText().toString().trim();
        String since = etSince.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        if (TextUtils.isEmpty(diseaseName) || TextUtils.isEmpty(since)) {
            Toast.makeText(getContext(), "Please fill all the fields for disease record", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = RadA1.isChecked() ? "Allergy" : "Disease";

        Disease disease = new Disease(diseaseName, since, type, description);

        diseaseList.add(disease);
        diseaseAdapter.notifyDataSetChanged();


    }

    private void saveMedicalRecords() throws Exception {
        String userId = mAuth.getCurrentUser().getUid();
        SecretKey key = EncryptionHelper.getStoredKey(getContext());
        FirestoreUtils.fetchEncryptedUserData(userId, key, getContext(), new FirestoreUtils.FirestoreCallback() {
            @Override
            public void onCallback(Map<String, Object> existingData) {
                try {
                    if (existingData == null || existingData.isEmpty()) {
                        existingData = new HashMap<>();
                        existingData.put("name", "PLEASEWORK!!!");
                        existingData.put("age", "69");
                        existingData.put("Records", new ArrayList<Map<String, String>>());
                    }
                    List<Map<String, String>> records = (List<Map<String, String>>) existingData.get("Records");
                    if (records == null) {
                        records = new ArrayList<>();
                    }
                    Map<String, String> newRecord = new HashMap<>();
                    newRecord.put("type", RadA1.isChecked() ? "Allergy" : "Disease");
                    newRecord.put("diseaseName", etDisease.getText().toString().trim());
                    newRecord.put("description", etDescription.getText().toString().trim());
                    newRecord.put("since", etSince.getText().toString().trim());
                    records.add(newRecord);

                    existingData.put("Records", records);

                    FirestoreUtils.saveEncryptedUserData(userId, existingData, getContext());
                    Log.d(TAG, "SAVED THIS BAD BOI" + existingData);

                    Toast.makeText(getContext(), "Medical record saved successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error saving medical record", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchMedicalRecords() {
        String userId = mAuth.getCurrentUser().getUid();
        SecretKey key;
        try {
            key = EncryptionHelper.getStoredKey(getContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FirestoreUtils.fetchEncryptedUserData(userId, key, getContext(), new FirestoreUtils.FirestoreCallback() {
            @Override
            public void onCallback(Map<String, Object> documentSnapshot) {
                if (documentSnapshot != null && !documentSnapshot.isEmpty()) {
                    Log.d(TAG, "ONCALLBACK" + documentSnapshot);
                    String name = (String) documentSnapshot.get("name");
                    List<Map<String, String>> records = (List<Map<String, String>>) documentSnapshot.get("Records");

                    nameOfkid.setText("Hi, " + name);

                    diseaseList.clear();

                    if (records != null) {
                        for (Map<String, String> record : records) {
                            String diseaseName = record.get("diseaseName");
                            String since = record.get("since");
                            String type = record.get("type");
                            String description = record.get("description");
                            Disease disease = new Disease(diseaseName, since, type, description);
                            Log.d(TAG, "VALUES"+ disease);
                            diseaseList.add(disease);
                        }
                    }

                    diseaseAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "No records found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
