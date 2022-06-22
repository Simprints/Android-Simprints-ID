package com.simprints.id.data.consent.longconsent.remote

import com.simprints.core.login.LoginInfoManager
import com.simprints.core.network.SimApiClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.file.FileUrl
import com.simprints.id.data.file.FileUrlRemoteInterface
import com.simprints.infra.logging.Simber

class LongConsentRemoteDataSourceImpl(
    private val loginInfoManager: LoginInfoManager,
    private val simApiClientFactory: SimApiClientFactory,
    private val consentDownloader: (FileUrl) -> ByteArray
) : LongConsentRemoteDataSource {

    private val projectId by lazy {
        loginInfoManager.getSignedInProjectIdOrEmpty()
    }

    override suspend fun downloadLongConsent(language: String): LongConsentRemoteDataSource.File {
        val fileId = "${LONG_CONSENT_FILE}_${language}"
        val longConsentRemoteApi = getLongConsentRemoteApi().api
        val fileUrl = longConsentRemoteApi.getFileUrl(projectId, fileId)
        Simber.d("Downloading long consent file at %s", fileUrl.url)
        val fileBytes = consentDownloader(fileUrl)
        return LongConsentRemoteDataSource.File(fileBytes)
    }

    private suspend fun getLongConsentRemoteApi(): SimApiClient<FileUrlRemoteInterface> {
        return simApiClientFactory.buildClient(FileUrlRemoteInterface::class)
    }

    companion object {
        private const val LONG_CONSENT_FILE = "privacy_notice"
    }
}
