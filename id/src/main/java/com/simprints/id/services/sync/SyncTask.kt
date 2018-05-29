package com.simprints.id.services.sync

import com.simprints.id.data.DataManager
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.service.ProgressTask
import io.reactivex.Observable

class SyncTask(private val dataManager: DataManager,
               private val parameters: SyncTaskParameters) : ProgressTask {

    override fun run(isInterrupted: () -> Boolean): Observable<Progress> = dataManager.db.sync(parameters, isInterrupted)
}
