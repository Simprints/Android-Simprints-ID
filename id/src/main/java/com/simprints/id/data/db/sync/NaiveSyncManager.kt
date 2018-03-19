package com.simprints.id.data.db.sync

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.JsonHelper
import com.simprints.libcommon.Progress
import io.reactivex.Observable

class NaiveSyncManager(private val firebaseToken: String,
                       private val localDbManager: LocalDbManager) {

    fun sync(syncParams: SyncTaskParameters, isInterrupted: () -> Boolean): Observable<Progress> =
        NaiveSync(
            SimApiClient(SyncApiInterface::class.java, SyncApiInterface.baseUrl).api,
            localDbManager,
            JsonHelper.gson,
            firebaseToken).sync(isInterrupted, syncParams)
}
