package com.dking.telladoc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dking.telladoc.essentials.EncryptionHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Base64;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private ImageView qrCodeImageView;
    private Button generateQRCodeButton;
    private Button scanQRcodeButton, SignOut;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        qrCodeImageView = view.findViewById(R.id.qrCodeImageView);
        generateQRCodeButton = view.findViewById(R.id.generateQRCodeButton);
        scanQRcodeButton = view.findViewById(R.id.scanQRcode);
        SignOut = view.findViewById(R.id.signOutbutton);

        generateQRCodeButton.setOnClickListener(v -> {
            try {
                generateQRCode(view.getContext());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        SignOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(view.getContext(), Login.class));
        });

        scanQRcodeButton.setOnClickListener(v -> {
            Intent registerIntent = new Intent(view.getContext(), ScanQRcode.class);
            startActivity(registerIntent);
        });

        return view;
    }



    private void generateQRCode(Context conteXt) throws Exception {

        String userId = mAuth.getCurrentUser().getUid() + "|#@>" + Base64.getEncoder().encodeToString(EncryptionHelper.getStoredKey(conteXt).getEncoded());

        try {

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(userId, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImageView.setImageBitmap(bitmap);

            Toast.makeText(getContext(), "QR Code generated successfully", Toast.LENGTH_SHORT).show();
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR Code: " + e.getMessage());
            Toast.makeText(getContext(), "Error generating QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
