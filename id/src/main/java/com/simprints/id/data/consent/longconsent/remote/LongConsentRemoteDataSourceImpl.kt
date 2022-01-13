package com.simprints.id.data.consent.longconsent.remote

import com.simprints.core.login.LoginInfoManager
import com.simprints.core.network.SimApiClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.logging.Simber
import java.net.URL

class LongConsentRemoteDataSourceImpl(
    private val loginInfoManager: LoginInfoManager,
    private val simApiClientFactory: SimApiClientFactory
) : LongConsentRemoteDataSource {

    private val projectId by lazy {
        loginInfoManager.getSignedInProjectIdOrEmpty()
    }

    override suspend fun downloadLongConsent(language: String): LongConsentRemoteDataSource.File {
        val fileId = "${LONG_CONSENT_FILE}_${language}"
        val longConsentRemoteApi = getLongConsentRemoteApi().api
        val fileUrl = longConsentRemoteApi.getLongConsentDownloadUrl(projectId, fileId)
        Simber.d("Downloading long consent file at %s", fileUrl.url)
        val fileBytes = URL(fileUrl.url).readBytes()
        return LongConsentRemoteDataSource.File(fileBytes)
    }

    private suspend fun getLongConsentRemoteApi(): SimApiClient<LongConsentRemoteInterface> {
        return simApiClientFactory.buildClient(LongConsentRemoteInterface::class)
    }

    companion object {
        private const val LONG_CONSENT_FILE = "privacy_notice"
    }
}
