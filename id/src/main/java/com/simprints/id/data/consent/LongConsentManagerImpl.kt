package com.simprints.id.data.consent

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.loginInfo.LoginInfoManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.File


class LongConsentManagerImpl(private val loginInfoManager: LoginInfoManager,
                             context: Context,
                             private val firebaseStorage: FirebaseStorage) : LongConsentManager {

    companion object {
        private const val FILE_PATH = "long-consents"
        private const val FILE_TYPE = "pdf"
    }

    private val filePath: File

    init {
        filePath = File(context.filesDir.absolutePath +
            File.separator +
            FILE_PATH +
            File.separator +
            loginInfoManager.signedInProjectId)

        if (!filePath.exists())
            filePath.mkdirs()
    }

    override fun downloadLongConsent(language: String):
        Flowable<Int> = Flowable.create<Int>({ emitter ->

        if (language.isBlank()) emitter.apply {
            onError(IllegalStateException("Invalid language choice: $language"))
            onComplete()
        }

        val file = File(filePath, "$language.$FILE_TYPE")

        firebaseStorage.getReference(FILE_PATH)
            .child(loginInfoManager.signedInProjectId)
            .child("$language.$FILE_TYPE")
            .getFile(file)
            .addOnSuccessListener {
                emitter.onComplete()
            }.addOnFailureListener {
                emitter.onError(it)
            }.addOnProgressListener {
                emitter.onNext(((it.bytesTransferred.toDouble() / it.totalByteCount.toDouble()) * 100).toInt())
            }

    }, BackpressureStrategy.BUFFER)

    override fun checkIfLongConsentExists(language: String): Boolean = File(filePath, "$language.$FILE_TYPE").exists()

    override fun getLongConsentUri(language: String): Uri {
        return Uri.fromFile(File(filePath, "$language.$FILE_TYPE"))
    }

}
