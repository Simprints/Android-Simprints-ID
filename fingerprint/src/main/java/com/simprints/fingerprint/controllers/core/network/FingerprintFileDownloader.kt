package com.simprints.fingerprint.controllers.core.network

import com.simprints.logging.Simber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class FingerprintFileDownloader {

    /** @throws IOException */
    suspend fun download(url: String): ByteArray = withContext(Dispatchers.IO) {
        // issue with timber logging URLs when interpolated in kotlin, check out this article
        // https://proandroiddev.com/be-careful-what-you-log-it-could-crash-your-app-5fc67a44c842
        Simber.d("Downloading firmware file at %s", url)
        URL(url).readBytes()
    }
}
