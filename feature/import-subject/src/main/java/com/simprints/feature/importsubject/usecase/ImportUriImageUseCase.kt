package com.simprints.feature.importsubject.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.simprints.core.DispatcherIO
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImportUriImageUseCase @Inject constructor(
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
    @ApplicationContext private val context: Context,
) {

    suspend operator fun invoke(uri: String): Bitmap? = withContext(dispatcherIO) {
        Simber.tag("POC").d("Importing uri: $uri")

        context.contentResolver
            .openInputStream(Uri.parse(uri))
            ?.use { Bitmap.createBitmap(BitmapFactory.decodeStream(it)) }
            ?.copy(Bitmap.Config.ARGB_8888, true)
            ?.also { Simber.tag("POC").d("Imported image: ${it.width}x${it.height}") }
    }
}
