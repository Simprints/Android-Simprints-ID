package com.simprints.id.sync

import com.google.gson.Gson
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.SyncExecutor
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Observable

class SyncExecutorMock(localDbManager: LocalDbManager,
                       remoteDbManager: RemoteDbManager,
                       gson: Gson) : SyncExecutor(localDbManager, remoteDbManager, gson) {

    public override fun uploadNewPatients(isInterrupted: () -> Boolean, batchSize: Int): Observable<Progress> {
        return super.uploadNewPatients(isInterrupted, batchSize)
    }

    public override fun downloadNewPatients(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        return super.downloadNewPatients(isInterrupted, syncParams)
    }
}
