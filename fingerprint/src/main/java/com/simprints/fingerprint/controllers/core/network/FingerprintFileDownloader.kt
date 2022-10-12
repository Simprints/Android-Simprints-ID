package com.simprints.fingerprint.controllers.core.network

import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import javax.inject.Inject

class FingerprintFileDownloader @Inject constructor(
    private val fingerprintApiClientFactory: FingerprintApiClientFactory,
    private val loginManager: LoginManager,
    private val dispatcherProvider: DispatcherProvider
) {


    private val projectId by lazy {
        loginManager.getSignedInProjectIdOrEmpty()
    }

    /** @throws IOException */
    suspend fun download(url: String): ByteArray = withContext(dispatcherProvider.io()) {
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
