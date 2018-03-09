package com.simprints.id.data.db.sync

import com.google.firebase.FirebaseApp
import com.simprints.libcommon.Progress
import com.simprints.id.libdata.tools.Utils
import io.reactivex.Emitter
import io.realm.RealmConfiguration

class NaiveSyncManager(firebaseApp: FirebaseApp,
                       private val legacyApiKey: String,
                       private val realmConfig: RealmConfiguration) {

    private val db = Utils.getDatabase(firebaseApp)

    fun syncUser(userId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>) =
            sync(userId, isInterrupted, emitter)

    fun syncGlobal(isInterrupted: () -> Boolean, emitter: Emitter<Progress>) =
            sync("", isInterrupted, emitter)

    private fun sync(userId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>) =
            NaiveSync(isInterrupted,
                emitter,
                userId,
                realmConfig,
                getProjRef(),
                getUsersRef(),
                getPatientsRef()).sync()

    private fun getProjRef() =
            db.getReference("projects/$legacyApiKey")

    private fun getUsersRef() =
            db.getReference("projects/$legacyApiKey/users")

    private fun getPatientsRef() =
            db.getReference("projects/$legacyApiKey/patients")
}
