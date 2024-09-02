package com.dking.telladoc.essentials;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeUtils {

    private static final String TAG = "QRCodeUtils";

    public static Bitmap generateQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR Code: " + e.getMessage());
            return null;
        }
    }
}
