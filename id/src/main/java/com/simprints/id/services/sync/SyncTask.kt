package com.simprints.id.services.sync

import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.services.progress.service.ProgressTask
import com.simprints.libcommon.Progress
import io.reactivex.Emitter
import kotlin.concurrent.thread

class SyncTask(private val dataManager: DataManager,
               private val parameters: SyncTaskParameters) : ProgressTask {

    override fun run(isInterrupted: () -> Boolean, emitter: Emitter<Progress>) {
        thread (start = true) {
            emitter.onNext(Progress(0, 0))
            try {
                sync(isInterrupted, emitter)
            } catch (error: UninitializedDataManagerError) {
                dataManager.logError(error)
                emitter.onError(error)
            }
        }
    }

    private fun sync(isInterrupted: () -> Boolean, emitter: Emitter<Progress>) {
        when (parameters) {
            is SyncTaskParameters.UserSyncTaskParameters -> {
                dataManager.syncUser(parameters.projectId, parameters.userId, isInterrupted, emitter)
            }
            is SyncTaskParameters.GlobalSyncTaskParameters ->
                dataManager.syncGlobal(parameters.projectId, isInterrupted, emitter)
        }
    }
}
