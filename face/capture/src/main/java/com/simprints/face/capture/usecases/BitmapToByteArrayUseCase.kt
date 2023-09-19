package com.simprints.face.capture.usecases

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import javax.inject.Inject

internal class BitmapToByteArrayUseCase @Inject constructor() {

    operator fun invoke(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}
