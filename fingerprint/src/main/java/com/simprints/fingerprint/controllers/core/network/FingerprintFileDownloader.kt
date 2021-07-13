package com.simprints.fingerprint.controllers.core.network

import com.simprints.logging.Simber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class FingerprintFileDownloader {

    /** @throws IOException */
    suspend fun download(url: String): ByteArray = withContext(Dispatchers.IO) {
        Simber.d("Downloading firmware file at $url")
        URL(url).readBytes()
    }
}
