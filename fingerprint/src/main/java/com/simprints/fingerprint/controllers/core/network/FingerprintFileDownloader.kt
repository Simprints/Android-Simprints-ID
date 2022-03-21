package com.simprints.fingerprint.controllers.core.network

import com.simprints.core.login.LoginInfoManager
import com.simprints.id.data.file.FileUrlRemoteInterface
import com.simprints.logging.Simber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class FingerprintFileDownloader(
    private val fingerprintApiClientFactory: FingerprintApiClientFactory,
    private val loginInfoManager: LoginInfoManager
    ) {


    private val projectId by lazy {
        loginInfoManager.getSignedInProjectIdOrEmpty()
    }
    /** @throws IOException */
    suspend fun download(url: String): ByteArray = withContext(Dispatchers.IO) {
        // issue with timber logging URLs when interpolated in kotlin, check out this article
        // https://proandroiddev.com/be-careful-what-you-log-it-could-crash-your-app-5fc67a44c842
        Simber.d("Downloading firmware file at %s", url)
        URL(url).readBytes()
    }

    suspend fun getFileUrl(fileId: String): String {
        val fileUrlRemoteApi = getFileUrlRemoteApi().api
        return fileUrlRemoteApi.getFileUrl(projectId, fileId).url
    }

    private suspend fun getFileUrlRemoteApi(): FingerprintApiClient<FileUrlRemoteInterface> {
        return fingerprintApiClientFactory.buildClient(FileUrlRemoteInterface::class)
    }

}
