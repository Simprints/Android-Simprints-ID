package com.simprints.fingerprint.tasks.saveperson

import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import io.reactivex.schedulers.Schedulers

class SavePersonTask(private val request: SavePersonTaskRequest,
                     private val dbManager: FingerprintDbManager) {

    fun savePerson() = SavePersonTaskResult(
        try {
            dbManager.savePerson(request.person)
                .subscribeOn(Schedulers.io())
                .blockingAwait()
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    )
}
