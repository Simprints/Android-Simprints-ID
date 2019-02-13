package com.simprints.id.data.consent

import android.content.Context
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.analytics.crashes.CrashReportManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import java.io.BufferedReader
import java.io.File

class LongConsentManagerImpl(context: Context,
                             private val loginInfoManager: LoginInfoManager,
                             private val crashReportManager: CrashReportManager) : LongConsentManager {

    companion object {
        private const val FILE_PATH = "long-consents"
        private const val FILE_TYPE = "txt"
        private const val DEFAULT_LANGUAGE = "en"

        private const val TIMEOUT_FAILURE_WINDOW_MILLIS = 1L
    }

    private val filePath: File
    private val firebaseStorage = FirebaseStorage.getInstance()

    init {
        filePath = createLocalFilePath(context)
    }

    private fun createLocalFilePath(context: Context): File {
        val filePath = File(context.filesDir.absolutePath +
            File.separator +
            FILE_PATH +
            File.separator +
            loginInfoManager.getSignedInProjectIdOrEmpty())

        if (!filePath.exists())
            filePath.mkdirs()

        return filePath
    }

    override fun downloadAllLongConsents(languages: Array<String>): Completable {
        val downloadTasks = mutableListOf<Completable>()
        languages.forEach { language ->
            if (!checkIfLongConsentExists(language))
                downloadTasks.add(
                    downloadLongConsentWithProgress(language)
                        .ignoreElements()
                        .doOnError { crashReportManager.logThrowable(it) }
                        .onErrorComplete()
                )
        }
        return Completable.mergeDelayError(downloadTasks)
    }

    override fun downloadLongConsentWithProgress(language: String): Flowable<Int> = Flowable.create<Int>(
        { emitter ->
            firebaseStorage.maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS
            val file = createFileForLanguage(language)
            getFileDownloadTask(language, file)
                .addOnSuccessListener {
                    emitter.onComplete()
                }.addOnFailureListener {
                    emitter.onError(it)
                }.addOnProgressListener {
                    emitter.onNext(((it.bytesTransferred.toDouble() / it.totalByteCount.toDouble()) * 100).toInt())
                }
        }, BackpressureStrategy.BUFFER)

    private fun getFileDownloadTask(language: String, file: File): FileDownloadTask =
        firebaseStorage.getReference(FILE_PATH)
            .child(loginInfoManager.getSignedInProjectIdOrEmpty())
            .child("$language.$FILE_TYPE")
            .getFile(file)

    override fun checkIfLongConsentExists(language: String): Boolean {
        val fileName = if (language.isEmpty()) {
            "$DEFAULT_LANGUAGE.$FILE_TYPE"
        } else {
            "$language.$FILE_TYPE"
        }

        return File(filePath, fileName).exists()
    }

    override fun getLongConsentText(language: String): String {

        val br: BufferedReader = createFileForLanguage(language).bufferedReader()
        val fileContent = StringBuffer("")

        br.forEachLine {
            fileContent.append(it + "\n")
        }

        return fileContent.toString()
    }

    private fun createFileForLanguage(language: String): File =
        File(filePath, "$language.$FILE_TYPE")
}
