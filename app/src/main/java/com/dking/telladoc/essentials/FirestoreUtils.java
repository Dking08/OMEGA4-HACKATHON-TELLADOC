package com.dking.telladoc.essentials;

import static com.dking.telladoc.essentials.EncryptionHelper.decrypt;
import static com.dking.telladoc.essentials.EncryptionHelper.encrypt;
//import static com.dking.telladoc.essentials.EncryptionHelper.gson;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

public class FirestoreUtils {

    private static final String TAG = "FirestoreUtils";
    private static final Gson gson = new Gson();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void saveEncryptedUserData(String userId, Map<String, Object> dataMap, Context context) {
        try {
            SecretKey key = EncryptionHelper.getStoredKey(context);
            Log.d(TAG,"DEAD"+key.toString());

            // Encrypt data map
            Map<String, Object> encryptedDataMap = EncryptionHelper.encryptMap(dataMap, key);

            // Save encrypted data to Firestore
            db.collection("users").document(userId).set(encryptedDataMap)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User data successfully encrypted and stored."))
                    .addOnFailureListener(e -> Log.e(TAG, "Error storing encrypted user data", e));
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting data", e);
        }
    }

    public static void saveEncryptedDocData(String userId, Map<String, Object> dataMap, Context context) {
        try {
            SecretKey key = EncryptionHelper.getStoredKey(context);
            Log.d(TAG,"DEAD"+key.toString());

            // Encrypt data map
            Map<String, Object> encryptedDataMap = EncryptionHelper.encryptMap(dataMap, key);

            // Save encrypted data to Firestore
            db.collection("doctors").document(userId).set(encryptedDataMap)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Doctor data successfully encrypted and stored."))
                    .addOnFailureListener(e -> Log.e(TAG, "Error storing encrypted user data", e));
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting data", e);
        }
    }

    public static Map<String, Object> encryptMap(Map<String, Object> dataMap, SecretKey key) throws Exception {
        Map<String, Object> encryptedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            if (entry.getValue() instanceof Map || entry.getValue() instanceof List) {
                String jsonValue = gson.toJson(entry.getValue());
                encryptedMap.put(entry.getKey(), encrypt(jsonValue, key));
            } else {
                encryptedMap.put(entry.getKey(), encrypt(entry.getValue().toString(), key));
            }
        }
        return encryptedMap;
    }

    public static Map<String, Object> decryptMap(Map<String, Object> encryptedMap, SecretKey key) throws Exception {
        Map<String, Object> decryptedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : encryptedMap.entrySet()) {
            String decryptedValue = decrypt(entry.getValue().toString(), key);
            try {
                // Try to parse as JSON (for nested structures)
                Object value = gson.fromJson(decryptedValue, Object.class);
                decryptedMap.put(entry.getKey(), value);
            } catch (JsonSyntaxException e) {
                // If not JSON, treat as simple string
                decryptedMap.put(entry.getKey(), decryptedValue);
            }
        }
        return decryptedMap;
    }

    public static void fetchEncryptedUserData(String userId,SecretKey key, Context context, FirestoreCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {

                            // Convert Firestore data back to a map
                            Map<String, Object> firestoreData = documentSnapshot.getData();
                            Map<String, Object> encryptedMap = new HashMap<>();
                            if (firestoreData != null) {
                                for (Map.Entry<String, Object> entry : firestoreData.entrySet()) {
                                    encryptedMap.put(entry.getKey(), entry.getValue().toString());
                                }
                            }

                            // Decrypt data
                            Map<String, Object> decryptedDataMap = EncryptionHelper.decryptMap(encryptedMap, key);
                            Log.d(TAG, "Decrypted data: " + decryptedDataMap);
                            callback.onCallback(decryptedDataMap);
                        } catch (Exception e) {
                            Log.e(TAG, "Error decrypting data", e);
                        }
                    } else {
                        Log.d(TAG, "No data found for user.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching encrypted user data", e));
    }

    public interface FirestoreCallback {
        void onCallback(Map<String, Object> decryptedDataMap);
    }

}