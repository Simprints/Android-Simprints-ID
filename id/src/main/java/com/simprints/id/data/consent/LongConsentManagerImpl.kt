package com.simprints.id.data.consent

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.prefs.PreferencesManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.File


class LongConsentManagerImpl(private val preferencesManager: PreferencesManager,
                             private val context: Context) {

    companion object {
        private const val FILE_PATH = "long-consents"
    }

    // Create a storage reference from our app
    val storage = FirebaseStorage.getInstance()
    var projectRef = storage.getReference(preferencesManager.projectId)

    fun downloadLongConsent(language: String):
        Flowable<Pair<Long, String>> = Flowable.create<Pair<Long, String>>({ emitter ->

        val file = File(context.filesDir,
            "$FILE_PATH/${preferencesManager.projectId}-$language.pdf")

        projectRef.child(language).getFile(file).addOnSuccessListener {
            emitter.onComplete()
        }.addOnFailureListener {
            emitter.onError(it)
        }.addOnProgressListener {
            emitter.onNext(Pair(it.bytesTransferred / it.totalByteCount, file.absolutePath))
        }

    }, BackpressureStrategy.BUFFER)

}
