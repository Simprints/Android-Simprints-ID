package com.simprints.fingerprint.tasks.saveperson

import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.di.FingerprintComponent
import com.simprints.id.data.prefs.PreferencesManager
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class SavePersonTask(component: FingerprintComponent,
                     private val request: SavePersonTaskRequest) {

    @Inject lateinit var dbManager: FingerprintDbManager
    @Inject lateinit var preferencesManager: PreferencesManager

    init {
        component.inject(this)
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
