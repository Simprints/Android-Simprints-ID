package com.simprints.id.data.consent.longconsent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import java.io.File

class LongConsentRepositoryImpl(private val longConsentLocalDataSource: LongConsentLocalDataSource,
                                private val loginInfoManager: LoginInfoManager,
                                private val crashReportManager: CrashReportManager) : LongConsentRepository {

    companion object {
        const val FILE_PATH = "long-consents"
        const val FILE_TYPE = "txt"
        const val DEFAULT_LANGUAGE = "en"
        private const val TIMEOUT_FAILURE_WINDOW_MILLIS = 1L
    }

    private val firebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val downloadProgress = MutableLiveData<Int>()
    private val isDownloadSuccessful = MutableLiveData<Boolean>(false)

    override suspend fun downloadLongConsentForLanguages(languages: Array<String>) {
        languages.forEach { language ->
            if (!checkIfLongConsentExistsInLocal(language))
                try {
                    downloadLongConsentWithProgress(language)
                } catch (t: Throwable) {
                    crashReportManager.logExceptionOrSafeException(t)
                }
        }
    }

    private fun checkIfLongConsentExistsInLocal(language: String) =
        longConsentLocalDataSource.checkIfLongConsentExistsInLocal(language)

    override suspend fun downloadLongConsentWithProgress(language: String): LiveData<Int> {
        firebaseStorage.maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS
        val file = longConsentLocalDataSource.createFileForLanguage(language)
        getFileDownloadTask(language, file)
            .addOnSuccessListener {
                isDownloadSuccessful.postValue(true)
            }
            .addOnProgressListener {
                downloadProgress.postValue(((it.bytesTransferred.toDouble() / it.totalByteCount.toDouble()) * 100).toInt())
            }

        return downloadProgress
    }

    private fun getFileDownloadTask(language: String, file: File): FileDownloadTask =
        firebaseStorage.getReference(FILE_PATH)
            .child(loginInfoManager.getSignedInProjectIdOrEmpty())
            .child("$language.${FILE_TYPE}")
            .getFile(file)

    override fun getLongConsentText(language: String) =
        longConsentLocalDataSource.getLongConsentText(language)

    override fun deleteLongConsents() {
        longConsentLocalDataSource.deleteLongConsents()
    }
}
