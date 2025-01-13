package com.simprints.id

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.simprints.core.AppScope
import com.simprints.core.CoreApplication
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.extentions.deviceHardwareId
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.DEVICE_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.logging.SimberBuilder
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("There is no complex business logic to test")
@HiltAndroidApp
open class Application :
    CoreApplication(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncOrchestrator: SyncOrchestrator

    @AppScope
    @Inject
    lateinit var appScope: CoroutineScope

    override fun attachBaseContext(base: Context) {
        LanguageHelper.init(base)
        val ctx = LanguageHelper.getLanguageConfigurationContext(base)
        super.attachBaseContext(ctx)
    }

    override fun onCreate() {
        super.onCreate()
        initApplication()
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration
            .Builder()
            .setWorkerFactory(workerFactory)
            .build()

    open fun initApplication() {
        SimberBuilder.initialize(this)
        Simber.setUserProperty(DEVICE_ID, deviceHardwareId)
        appScope.launch {
            syncOrchestrator.cleanupWorkers()
            syncOrchestrator.scheduleBackgroundWork()
        }
    }
}
