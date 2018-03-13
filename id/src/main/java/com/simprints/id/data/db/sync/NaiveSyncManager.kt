package com.simprints.id.data.db.sync

import com.google.firebase.FirebaseApp
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.libcommon.Progress
import io.reactivex.Observable
import io.realm.RealmConfiguration

class NaiveSyncManager(firebaseApp: FirebaseApp,
                       private val realmConfig: RealmConfiguration) {

    fun sync(paramenters: SyncTaskParameters, isInterrupted: () -> Boolean): Observable<Progress> =
        NaiveSync(
            isInterrupted,
            paramenters.toGroup(),
            NaiveSyncConnectorImp(paramenters),
            realmConfig).sync()
}
