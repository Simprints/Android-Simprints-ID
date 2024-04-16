package com.simprints.feature.importsubject.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.simprints.core.DispatcherIO
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImportBase64ImageUseCase @Inject constructor(
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) {

    suspend operator fun invoke(image: String): Bitmap? = withContext(dispatcherIO) {
        Simber.tag("POC").d("Importing image")

        val decodedBytes = Base64.decode(image, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
