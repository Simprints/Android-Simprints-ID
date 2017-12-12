package com.simprints.id.services.sync

import com.simprints.id.data.DataManager
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.service.ProgressTask
import com.simprints.id.services.sync.SyncTaskParameters.GlobalSyncTaskParameters
import com.simprints.id.services.sync.SyncTaskParameters.UserSyncTaskParameters
import com.simprints.id.throwables.safe.InterruptedSyncException
import com.simprints.id.throwables.unsafe.UnexpectedSyncError
import com.simprints.libdata.DATA_ERROR
import com.simprints.libdata.DataCallback
import io.reactivex.Emitter
import kotlin.concurrent.thread


class SyncTask(private val dataManager: DataManager,
               private val parameters: SyncTaskParameters) : ProgressTask {

    override fun run(isInterrupted: () -> Boolean, emitter: Emitter<Progress>) {
        thread (start = true) {
            emitter.onNext(Progress(0, 0))
            when (parameters) {
                is UserSyncTaskParameters ->
                    dataManager.syncUser(parameters.appKey, parameters.userId, wrapEmitterInDataCallback(emitter))
                is GlobalSyncTaskParameters ->
                    dataManager.syncGlobal(parameters.appKey, wrapEmitterInDataCallback(emitter))
            }
        }
    }

    private fun wrapEmitterInDataCallback(emitter: Emitter<Progress>) =
            object : DataCallback {
                override fun onSuccess() {
                    emitter.onComplete()
                }

                override fun onFailure(dataError: DATA_ERROR) {
                    emitter.onError(
                            when (dataError) {
                                DATA_ERROR.SYNC_INTERRUPTED -> InterruptedSyncException()
                                else -> UnexpectedSyncError()
                            }
                    )
                }
            }

}
