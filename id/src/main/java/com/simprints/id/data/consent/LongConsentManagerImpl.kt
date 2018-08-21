package com.simprints.id.data.consent

import android.content.Context
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.R
import com.simprints.id.data.loginInfo.LoginInfoManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.BufferedReader
import java.io.File

class LongConsentManagerImpl(context: Context,
                             private val loginInfoManager: LoginInfoManager) : LongConsentManager {

    companion object {
        private const val FILE_PATH = "long-consents"
        private const val FILE_TYPE = "txt"
        private const val DEFAULT_LANGUAGE = "en"

        private const val TIMEOUT_FAILURE_WINDOW_MILLIS = 1L
    }

    private val filePath: File
    private val firebaseStorage = FirebaseStorage.getInstance() // TODO : migrate to FirebaseManager whenever it exists

    init {
        filePath = File(context.filesDir.absolutePath +
            File.separator +
            FILE_PATH +
            File.separator +
            loginInfoManager.getSignedInProjectIdOrEmpty())

        if (!filePath.exists())
            filePath.mkdirs()
    }

    override fun downloadLongConsent(language: String):
        Flowable<Int> = Flowable.create<Int>({ emitter ->

        if (language.isBlank()) emitter.apply {
            onError(IllegalStateException("Invalid language choice: $language"))
        }

        val file = File(filePath, "$language.$FILE_TYPE")

        firebaseStorage.maxDownloadRetryTimeMillis = TIMEOUT_FAILURE_WINDOW_MILLIS

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

        val br: BufferedReader = File(filePath, "$language.$FILE_TYPE").bufferedReader()
        val fileContent = StringBuffer("")

        br.forEachLine {
            fileContent.append(it + "\n")
        }

        return fileContent.toString()
    }

    override val languages: Array<String> = context.resources.getStringArray(R.array.language_values)
        ?: arrayOf(DEFAULT_LANGUAGE)
}
