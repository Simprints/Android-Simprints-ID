package com.simprints.id.data.db.sync

import com.google.firebase.FirebaseApp
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.JsonHelper
import com.simprints.libcommon.Progress
import io.reactivex.Observable
import io.realm.RealmConfiguration

class NaiveSyncManager(firebaseApp: FirebaseApp,
                       private val realmConfig: RealmConfiguration,
                       private val localDbManager: LocalDbManager) {

    fun sync(syncParams: SyncTaskParameters, isInterrupted: () -> Boolean): Observable<Progress> =
        NaiveSync(SimApi().api, localDbManager, JsonHelper.gson).sync(isInterrupted, syncParams)
}
