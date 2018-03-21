package com.simprints.id.sync

import com.google.gson.Gson
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.NaiveSync
import com.simprints.id.data.db.sync.SyncApiInterface
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Observable
import io.reactivex.Single
import java.util.ArrayList


class NaiveSyncMock(api: SyncApiInterface,
                    localDbManager: LocalDbManager,
                    gson: Gson) : NaiveSync(api, localDbManager, gson) {

    public override fun makeUploadPatientsBatchRequest(patientsToUpload: ArrayList<fb_Person>): Single<Int> {
        return super.makeUploadPatientsBatchRequest(patientsToUpload)
    }

    public override fun uploadNewPatients(isInterrupted: () -> Boolean, batchSize: Int): Observable<Progress> {
        return super.uploadNewPatients(isInterrupted, batchSize)
    }

    public override fun downloadNewPatients(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        return super.downloadNewPatients(isInterrupted, syncParams)
    }

    public override fun getNumberOfPatientsForSyncParams(syncParams: SyncTaskParameters): Single<Int> {
        return super.getNumberOfPatientsForSyncParams(syncParams)
    }
}
