package com.dking.telladoc.essentials;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.dking.telladoc.BuildConfig;
import com.google.gson.Gson;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionHelper {

    private static final String KEY_ALIAS = "healthsync_key";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";

    private static final String SALT = BuildConfig.SALT;
    private static final int GCM_TAG_LENGTH = 16;
    private static final Gson gson = new Gson();

    public static SecretKey deriveKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), SALT.getBytes(), 10000, 256);
        return factory.generateSecret(spec);
    }


    public static SecretKey getSecretKey(Context context) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build();

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        }

        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
    }

    public static String encrypt(String plainText, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] iv = cipher.getIV(); // Initialization vector
        byte[] encryptedData = cipher.doFinal(plainText.getBytes("UTF-8"));

        // Combine IV and encrypted data for storage
        byte[] combinedData = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combinedData, 0, iv.length);
        System.arraycopy(encryptedData, 0, combinedData, iv.length, encryptedData.length);

        return Base64.encodeToString(combinedData, Base64.DEFAULT);
    }

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_MODE);

        byte[] decodedData = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] iv = new byte[12]; // GCM standard uses 12-byte IV
        byte[] encryptedBytes = new byte[decodedData.length - iv.length];

        System.arraycopy(decodedData, 0, iv, 0, iv.length);
        System.arraycopy(decodedData, iv.length, encryptedBytes, 0, encryptedBytes.length);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, "UTF-8");
    }

    public static Map<String, Object> encryptMap(Map<String, Object> dataMap, SecretKey key) throws Exception {
        Map<String, Object> encryptedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String jsonValue = gson.toJson(entry.getValue()); // Serialize object to JSON string
            encryptedMap.put(entry.getKey(), encrypt(jsonValue, key));
        }
        return encryptedMap;
    }

    public static void storeEncryptedKey(Context context, String password) throws Exception {
        SecretKey derivedKey = deriveKey(password);
        SecretKey keyStoreKey = getKeyStoreKey(context);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keyStoreKey);
        byte[] encryptedKey = cipher.doFinal(derivedKey.getEncoded());
        byte[] iv = cipher.getIV();

        // Store encryptedKey and iv in SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("EncryptedKeyPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("encryptedKey", Base64.encodeToString(encryptedKey, Base64.DEFAULT))
                .putString("iv", Base64.encodeToString(iv, Base64.DEFAULT))
                .apply();
    }

    public static SecretKey getKeyStoreKey(Context context) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build();
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        }
        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
    }

    public static SecretKey getStoredKey(Context context) throws Exception {
        SecretKey keyStoreKey = getKeyStoreKey(context);

        SharedPreferences prefs = context.getSharedPreferences("EncryptedKeyPrefs", Context.MODE_PRIVATE);
        String encryptedKeyB64 = prefs.getString("encryptedKey", null);
        String ivB64 = prefs.getString("iv", null);

        if (encryptedKeyB64 == null || ivB64 == null) {
            throw new Exception("No stored key found");
        }

        byte[] encryptedKey = Base64.decode(encryptedKeyB64, Base64.DEFAULT);
        byte[] iv = Base64.decode(ivB64, Base64.DEFAULT);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, keyStoreKey, spec);
        byte[] decryptedKey = cipher.doFinal(encryptedKey);

        return new SecretKeySpec(decryptedKey, "AES");
    }


    public static Map<String, Object> decryptMap(Map<String, Object> encryptedMap, SecretKey key) throws Exception {
        Map<String, Object> decryptedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : encryptedMap.entrySet()) {
            String decryptedJson = decrypt(entry.getValue().toString(), key);
            Object value = gson.fromJson(decryptedJson, Object.class); // Deserialize JSON string back to object
            decryptedMap.put(entry.getKey(), value);
        }
        return decryptedMap;
    }
}
