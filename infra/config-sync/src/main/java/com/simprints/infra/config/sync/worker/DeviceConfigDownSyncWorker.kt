package com.simprints.infra.config.sync.worker

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.sync.usecase.LogoutUseCase
import com.simprints.infra.enrolment.records.sync.worker.EnrolmentRecordScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class DeviceConfigDownSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val configRepository: ConfigRepository,
    private val logoutUseCase: LogoutUseCase,
    private val enrolmentRecordScheduler: EnrolmentRecordScheduler,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {

    override val tag: String = "DeviceConfigSyncWorker"

    override suspend fun doWork(): Result =
        withContext(dispatcher) {
            crashlyticsLog("Fetching device config state")
            showProgressNotification()

            try {
                val state = configRepository.getDeviceState()

                if (state.isCompromised) {
                    logoutUseCase()
                } else if (state.recordsToUpSync != null) {
                    state.recordsToUpSync?.let { records ->
                        crashlyticsLog("subject ids ${records.subjectIds.size}")
                        enrolmentRecordScheduler.upload(records.id, records.subjectIds)
                    }
                }
                success()
            } catch (t: Throwable) {
                fail(t)
            }
        }
}
