package com.simprints.infra.sync.devicestate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.sync.devicestate.ReportDeviceDataStateUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Periodically reports the device data state to the backend, independently of any other sync.
 *
 * Failures are not retried: the report is a snapshot, so the next scheduled run sends a fresher one.
 */
@HiltWorker
internal class DeviceStatusReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reportDeviceDataState: ReportDeviceDataStateUseCase,
    @param:DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {
    override val tag: String = "DeviceStatusReport"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Started")
        try {
            if (reportDeviceDataState()) {
                success()
            } else {
                success(message = "No signed-in project")
            }
        } catch (t: Throwable) {
            fail(t)
        }
    }
}
