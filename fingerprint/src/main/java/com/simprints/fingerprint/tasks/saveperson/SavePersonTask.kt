package com.simprints.fingerprint.tasks.saveperson

import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import io.reactivex.schedulers.Schedulers
import java.util.*

class SavePersonTask(private val request: SavePersonTaskRequest,
                     private val dbManager: FingerprintDbManager,
                     private val preferencesManager: FingerprintPreferencesManager) {

    fun savePerson() = SavePersonTaskResult(
        try {
            dbManager.savePerson(request.person)
                .subscribeOn(Schedulers.io())
                .blockingAwait()
            preferencesManager.lastEnrolDate = Date()
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    )
}
