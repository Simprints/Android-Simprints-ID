package com.simprints.id.data.db.sync

import com.google.firebase.FirebaseApp
import com.simprints.libcommon.Progress
import io.reactivex.Emitter
import io.realm.RealmConfiguration

class NaiveSyncManager(firebaseApp: FirebaseApp,
                       private val projectId: String,
                       private val realmConfig: RealmConfiguration) {

    fun syncUser(userId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>) =
        sync(null, userId, isInterrupted, emitter)

    fun syncGlobal(isInterrupted: () -> Boolean, emitter: Emitter<Progress>) =
        sync(null, null, isInterrupted, emitter)

    private fun sync(moduleId: String?,
                     userId: String?, isInterrupted: () -> Boolean, emitter: Emitter<Progress>) =
        NaiveSync(isInterrupted,
            emitter,
            projectId,
            moduleId,
            userId,
            realmConfig).sync()
}
