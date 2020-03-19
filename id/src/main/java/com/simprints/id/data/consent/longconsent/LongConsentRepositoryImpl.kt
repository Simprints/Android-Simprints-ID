package com.simprints.id.data.consent.longconsent

import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    private var language = DEFAULT_LANGUAGE

    override val downloadProgress = MutableLiveData<Int>()
    override val isDownloadSuccessful = MutableLiveData<Boolean>()
    override val longConsentText = MutableLiveData<String>()

    override fun setLanguage(language: String) {
        this.language = language
        updateLongConsentText()
    }

    override suspend fun downloadLongConsentForLanguages(languages: Array<String>) {
        languages.forEach { language ->
            if (!checkIfLongConsentExistsInLocal(language))
                try {
                    downloadLongConsentWithoutProgress(language)
                } catch (t: Throwable) {
                    crashReportManager.logExceptionOrSafeException(t)
                }
        }
    }

    private fun checkIfLongConsentExistsInLocal(language: String) =
        longConsentLocalDataSource.isLongConsentPresentInLocal(language)

    private fun downloadLongConsentWithoutProgress(language: String) {
        firebaseStorage.maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS
        val file = longConsentLocalDataSource.createFileForLanguage(language)
        getFileDownloadTask(language, file).resume()
    }

    override suspend fun downloadLongConsentWithProgress() {
        firebaseStorage.maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS
        val file = longConsentLocalDataSource.createFileForLanguage(language)
        withContext(Dispatchers.IO) {
            getFileDownloadTask(language, file)
                .addOnProgressListener {
                    downloadProgress.postValue(((it.bytesTransferred.toDouble() / it.totalByteCount.toDouble()) * 100).toInt())
                }
                .addOnFailureListener {
                    isDownloadSuccessful.postValue(false)
                }
                .addOnSuccessListener {
                    isDownloadSuccessful.postValue(true)
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
        longConsentText.value = longConsentLocalDataSource.getLongConsentText(language)
    }

    override fun deleteLongConsents() {
        longConsentLocalDataSource.deleteLongConsents()
    }
}
