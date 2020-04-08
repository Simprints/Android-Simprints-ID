package com.simprints.id.data.consent.longconsent

import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentLocalDataSourceImpl.Companion.FILE_PATH
import com.simprints.id.data.consent.longconsent.LongConsentLocalDataSourceImpl.Companion.FILE_TYPE
import com.simprints.id.data.loginInfo.LoginInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LongConsentRepositoryImpl(private val longConsentLocalDataSource: LongConsentLocalDataSource,
                                private val loginInfoManager: LoginInfoManager,
                                private val crashReportManager: CrashReportManager) : LongConsentRepository {

    companion object {
        const val DEFAULT_LANGUAGE = "en"
        private const val TIMEOUT_FAILURE_WINDOW_MILLIS = 1L
    }

    private val bucketUrl = if(BuildConfig.DEBUG) "gs://simprints-dev-firebase-storage" else "gs://simprints-152315-firebase-storage"

    private val firebaseStorage by lazy { FirebaseStorage.getInstance(bucketUrl) }
    internal var language = DEFAULT_LANGUAGE

    override val downloadProgressLiveData = MutableLiveData<Int>()
    override val isDownloadSuccessfulLiveData = MutableLiveData<Boolean>()
    override val longConsentTextLiveData = MutableLiveData<String>()

    override fun setLanguage(language: String) {
        this.language = language
        updateLongConsentText()
    }

    override suspend fun downloadLongConsentForLanguages(languages: Array<String>) {
        languages.forEach { language ->
            if (!isLongConsentPresentInLocal(language))
                try {
                    downloadLongConsentWithoutProgress(language)
                } catch (t: Throwable) {
                    crashReportManager.logExceptionOrSafeException(t)
                }
        }
    }

    private fun isLongConsentPresentInLocal(language: String) =
        longConsentLocalDataSource.isLongConsentPresentInLocal(language)

    private suspend fun downloadLongConsentWithoutProgress(language: String) {
        firebaseStorage.maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS
        val file = longConsentLocalDataSource.createFileForLanguage(language)
        withContext(Dispatchers.IO) {
            getFileDownloadTask(language, file).resume()
        }
    }

    override suspend fun downloadLongConsentWithProgress() {
        firebaseStorage.maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS
        val file = longConsentLocalDataSource.createFileForLanguage(language)
        withContext(Dispatchers.IO) {
            getFileDownloadTask(language, file)
                .addOnProgressListener {
                    downloadProgressLiveData.postValue(((it.bytesTransferred.toDouble() / it.totalByteCount.toDouble()) * 100).toInt())
                }
                .addOnFailureListener {
                    isDownloadSuccessfulLiveData.postValue(false)
                }
                .addOnSuccessListener {
                    isDownloadSuccessfulLiveData.postValue(true)
                    updateLongConsentText()
                }.resume()
        }
    }

    private fun getFileDownloadTask(language: String, file: File): FileDownloadTask =
        firebaseStorage.getReference(FILE_PATH)
            .child(loginInfoManager.getSignedInProjectIdOrEmpty())
            .child("$language.${FILE_TYPE}")
            .getFile(file)

    private fun updateLongConsentText() {
        longConsentTextLiveData.value = longConsentLocalDataSource.getLongConsentText(language)
    }

    override fun deleteLongConsents() {
        longConsentLocalDataSource.deleteLongConsents()
    }
}
