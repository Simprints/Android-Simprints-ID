package com.simprints.id.data.consent

import android.os.Handler
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import java.io.BufferedReader
import java.io.File
import android.os.Looper
import com.google.android.gms.tasks.Tasks
import timber.log.Timber


class LongConsentManagerImpl(absolutePath: String,
                             private val loginInfoManager: LoginInfoManager,
                             private val crashReportManager: CrashReportManager) : LongConsentManager {

    companion object {
        private const val FILE_PATH = "long-consents"
        private const val FILE_TYPE = "txt"
        private const val DEFAULT_LANGUAGE = "en"

        private const val TIMEOUT_FAILURE_WINDOW_MILLIS = 1L
    }

    internal val baseFilePath: File by lazy {
        createBaseFilePath(absolutePath)
    }

    internal val filePathForProject: File by lazy {
        createLocalFilePath(baseFilePath.absolutePath)
    }

    private val firebaseStorage = FirebaseStorage.getInstance()

    private fun createBaseFilePath(absolutePath: String) = File (absolutePath +
        File.separator +
        FILE_PATH)

    internal fun createLocalFilePath(absolutePath: String): File {
        val filePath = File(absolutePath +
            File.separator +
            loginInfoManager.getSignedInProjectIdOrEmpty())

        if (!filePath.exists()) {
            filePath.mkdirs()
        }

        return filePath
    }

    override fun downloadAllLongConsents(languages: Array<String>): Completable {
        val downloadTasks = mutableListOf<Completable>()
        languages.forEach { language ->
            if (!checkIfLongConsentExists(language))
                downloadTasks.add(
                    downloadLongConsentWithProgress(language)
                        .ignoreElements()
                        .doOnError {
                            Timber.e(it)
                            crashReportManager.logExceptionOrSafeException(it) }
                        .onErrorComplete()
                )
        }
        return Completable.mergeDelayError(downloadTasks)
    }

    override fun downloadLongConsentWithProgress(language: String): Flowable<Int> = Flowable.create<Int>(
        { emitter ->
            firebaseStorage.maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS
            val file = createFileForLanguage(filePathForProject, language)
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

        return File(filePathForProject, fileName).exists()
    }

    override fun getLongConsentText(language: String): String {

        val br: BufferedReader = createFileForLanguage(filePathForProject, language).bufferedReader()
        val fileContent = StringBuffer("")

        br.forEachLine {
            fileContent.append(it + "\n")
        }

        return fileContent.toString()
    }

    internal fun createFileForLanguage(parentLanguageFilePath: File, language: String): File =
        File(parentLanguageFilePath, "$language.$FILE_TYPE")

    override fun deleteLongConsents() {
        getAllLongConsentFiles()?.forEach { baseFile ->
            if(baseFile.isDirectory) {
                deleteFilesInDirectory(baseFile)
            }
            baseFile.delete()
        }
    }

    private fun getAllLongConsentFiles() =
        baseFilePath.listFiles()

    private fun deleteFilesInDirectory(baseFile: File) {
        baseFile.listFiles().forEach { it.delete() }
    }
}
