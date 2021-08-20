package com.simprints.id.data.consent.longconsent

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StreamDownloadTask
import com.simprints.core.login.LoginInfoManager
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.tools.extensions.awaitTask

class LongConsentRemoteDataSourceImpl(
    private val loginInfoManager: LoginInfoManager,
    private val remoteDbManager: RemoteDbManager
) :
    LongConsentRemoteDataSource {

    companion object {
        private const val TIMEOUT_FAILURE_WINDOW_MILLIS = 1L
    }

    override suspend fun downloadLongConsent(language: String): LongConsentRemoteDataSource.Stream =
        getFileDownloadTask(language).awaitTask().let {
            LongConsentRemoteDataSource.Stream(it.stream, it.totalByteCount)
        }

    private fun getFileDownloadTask(language: String): StreamDownloadTask =
        getFirebaseStorage().getReference(LongConsentLocalDataSourceImpl.FILE_PATH)
            .child(loginInfoManager.getSignedInProjectIdOrEmpty())
            .child("$language.${LongConsentLocalDataSourceImpl.FILE_TYPE}")
            .stream

    private fun getFirebaseStorage() =
        FirebaseStorage.getInstance(remoteDbManager.getLegacyAppFallback(), BuildConfig.LONG_CONSENT_BUCKET).apply {
            maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS
        }

}
