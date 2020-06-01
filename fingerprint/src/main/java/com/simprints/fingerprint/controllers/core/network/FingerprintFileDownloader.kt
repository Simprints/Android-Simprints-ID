package com.simprints.fingerprint.controllers.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.URL

class FingerprintFileDownloader {

    /** @throws IOException */
    suspend fun download(url: String): ByteArray = withContext(Dispatchers.IO) {
        Timber.d("Downloading firmware file at $url")
        URL(url).readBytes()
    }
}
