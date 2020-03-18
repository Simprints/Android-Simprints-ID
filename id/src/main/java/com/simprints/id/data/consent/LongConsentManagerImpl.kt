package com.simprints.id.data.consent

import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.File


open class LongConsentManagerImpl(absolutePath: String,
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

    private val firebaseStorage by lazy { FirebaseStorage.getInstance() }

    private fun createBaseFilePath(absolutePath: String) = File (absolutePath +
        File.separator +
        FILE_PATH)

    private fun createLocalFilePath(absolutePath: String): File {
        val filePath = File(absolutePath +
            File.separator +
            loginInfoManager.getSignedInProjectIdOrEmpty())

        if (!filePath.exists()) {
            filePath.mkdirs()
        }

        return filePath
    }

    override suspend fun downloadAllLongConsents(languages: Array<String>) {
        languages.forEach { language ->
            if (!checkIfLongConsentExistsInLocal(language))
                try {
                    downloadLongConsentWithProgress(language)
                } catch (t: Throwable) {
                    crashReportManager.logExceptionOrSafeException(t)
                }
        }
    }

    override suspend fun downloadLongConsentWithProgress(language: String): Flow<Int> = flow {
            firebaseStorage.maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS
            val file = createFileForLanguage(filePathForProject, language)
            getFileDownloadTask(language, file)
                .addOnProgressListener {
                    //emit(((it.bytesTransferred.toDouble() / it.totalByteCount.toDouble()) * 100).toInt())
                }
        }

    private fun getFileDownloadTask(language: String, file: File): FileDownloadTask =
        firebaseStorage.getReference(FILE_PATH)
            .child(loginInfoManager.getSignedInProjectIdOrEmpty())
            .child("$language.$FILE_TYPE")
            .getFile(file)

    override fun checkIfLongConsentExistsInLocal(language: String): Boolean {
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
