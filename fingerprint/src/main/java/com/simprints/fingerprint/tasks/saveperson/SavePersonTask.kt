package com.simprints.fingerprint.tasks.saveperson

import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.id.data.prefs.PreferencesManager
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class SavePersonTask(private val request: SavePersonTaskRequest) {

    @Inject lateinit var dbManager: FingerprintDbManager
    @Inject lateinit var preferencesManager: PreferencesManager

    init {
        FingerprintComponentBuilder.getComponent()?.inject(this) ?: throw Throwable("Woops") // TODO
    }

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
