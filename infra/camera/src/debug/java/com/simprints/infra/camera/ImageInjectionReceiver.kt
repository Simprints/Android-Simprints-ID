package com.simprints.infra.camera

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import com.simprints.infra.camera.repository.InjectedImageCache
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ImageInjectionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var cache: InjectedImageCache

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val extras = intent.extras
        Simber.d("Image injection broadcast received. Extras: ${extras?.keySet()?.joinToString { "$it=${extras[it]}" }}")
        val filename = intent.getStringExtra(EXTRA_FILE)
        if (filename.isNullOrBlank()) {
            cache.injectedImage = null
            Simber.d("Image injection broadcast does not contain '$EXTRA_FILE' extra. Image injection cleared")
            return
        }
        val dir = context.applicationContext.getExternalFilesDir(null)
        if (dir == null) {
            cache.injectedImage = null
            Simber.d("External files dir cannot be resolved. Image injection cleared.")
            return
        }
        val bitmap = BitmapFactory.decodeFile(File(dir, filename).absolutePath)
        if (bitmap != null) {
            cache.injectedImage = bitmap
            val message = "Image injected: $filename"
            Simber.d(message)
            Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
        } else {
            cache.injectedImage = null
            Simber.d("failed to decode '$filename'. Image injection cleared")
        }
    }

    companion object {
        private const val EXTRA_FILE = "frame"
    }
}
