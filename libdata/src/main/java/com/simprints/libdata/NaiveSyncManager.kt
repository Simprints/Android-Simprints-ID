package com.simprints.libdata

import com.google.firebase.FirebaseApp
import com.simprints.libcommon.Progress
import com.simprints.libdata.tools.Utils
import io.reactivex.Emitter
import io.realm.RealmConfiguration

class NaiveSyncManager(firebaseApp: FirebaseApp,
                       private val legacyKey: String,
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
            db.getReference("projects/$legacyKey")

    private fun getUsersRef() =
            db.getReference("projects/$legacyKey/users")

    private fun getPatientsRef() =
            db.getReference("projects/$legacyKey/patients")
}
