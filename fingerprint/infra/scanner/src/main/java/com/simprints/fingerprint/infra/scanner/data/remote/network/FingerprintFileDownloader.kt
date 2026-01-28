package com.simprints.fingerprint.infra.scanner.data.remote.network

import com.simprints.core.DispatcherIO
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import javax.inject.Inject

/**
 * This class is responsible for downloading the firmware updates.
 */
internal class FingerprintFileDownloader @Inject constructor(
    private val backendApiClient: BackendApiClient,
    private val authStore: AuthStore,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    private val projectId by lazy {
        authStore.signedInProjectId
    }

    /**
     * This method downloads the firmware bytes using the provided url
     *
     * @param url  the url location for the file to be downloaded
     * @return the bytes of the downloaded file
     *
     * @throws IOException
     */
    suspend fun download(url: String): ByteArray = withContext(dispatcher) {
        // issue with timber logging URLs when interpolated in kotlin, check out this article
        // https://proandroiddev.com/be-careful-what-you-log-it-could-crash-your-app-5fc67a44c842
        Simber.d("Downloading firmware file at $url")
        URL(url).readBytes()
    }

    suspend fun getFileUrl(fileId: String): String = backendApiClient
        .executeCall(FileUrlRemoteInterface::class) { api ->
            api.getFileUrl(projectId, fileId).url
        }.getOrThrow()
}
