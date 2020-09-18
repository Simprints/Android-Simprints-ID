package com.simprints.core.tools;


import android.util.Base64;

import androidx.annotation.NonNull;

// For some reason, mocking this class in Kotlin fails (required by Fingerprint module)
public class EncodingUtils {

    public static @NonNull //Do not use in unit test - android.util.Base64 requires android sdk
    String byteArrayToBase64(@NonNull byte[] bytes)
    {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static @NonNull byte[] base64ToBytes(@NonNull String base64) throws IllegalArgumentException
    {
        return Base64.decode(base64, Base64.DEFAULT);
    }
}
