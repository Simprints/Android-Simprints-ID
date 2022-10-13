package com.simprints.id.data.consent.longconsent.remote

import com.simprints.id.data.file.FileUrl
import com.simprints.id.data.file.FileUrlRemoteInterface
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork
import java.net.URL
import javax.inject.Inject

class LongConsentRemoteDataSourceImpl(
    private val loginManager: LoginManager,
    private val consentDownloader: (FileUrl) -> ByteArray
) : LongConsentRemoteDataSource {

    @Inject
    constructor(
        loginManager: LoginManager
    ) : this(loginManager, { fileUrl -> URL(fileUrl.url).readBytes() })

    private val projectId by lazy {
        loginManager.getSignedInProjectIdOrEmpty()
    }

    override suspend fun downloadLongConsent(language: String): LongConsentRemoteDataSource.File {
        val fileId = "${LONG_CONSENT_FILE}_${language}"
        val longConsentRemoteApi = getLongConsentRemoteApi().api
        val fileUrl = longConsentRemoteApi.getFileUrl(projectId, fileId)
        Simber.d("Downloading long consent file at %s", fileUrl.url)
        val fileBytes = consentDownloader(fileUrl)
        return LongConsentRemoteDataSource.File(fileBytes)
    }

    private suspend fun getLongConsentRemoteApi(): SimNetwork.SimApiClient<FileUrlRemoteInterface> {
        return loginManager.buildClient(FileUrlRemoteInterface::class)
    }

    companion object {
        private const val LONG_CONSENT_FILE = "privacy_notice"
    }
}
