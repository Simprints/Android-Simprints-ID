package com.simprints.fingerprint.controllers.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class FingerprintFileDownloader {

    /** @throws IOException */
    suspend fun download(url: String): ByteArray = withContext(Dispatchers.IO) {
        URL(url).readBytes()
    }
}
