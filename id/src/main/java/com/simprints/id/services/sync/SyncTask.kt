package com.simprints.id.services.sync

import com.simprints.id.data.db.DbManager
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.service.ProgressTask
import io.reactivex.Observable

class SyncTask(private val dbManager: DbManager,
               private val parameters: SyncTaskParameters) : ProgressTask {

    override fun run(isInterrupted: () -> Boolean): Observable<Progress> = dbManager.sync(parameters, isInterrupted)
}
