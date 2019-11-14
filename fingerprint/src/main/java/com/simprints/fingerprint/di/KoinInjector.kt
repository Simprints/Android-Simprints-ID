package com.simprints.fingerprint.di

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.simprints.fingerprint.activities.alert.AlertContract
import com.simprints.fingerprint.activities.alert.AlertPresenter
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.collect.CollectFingerprintsContract
import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.matching.MatchingViewModel
import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel
import com.simprints.fingerprint.activities.refusal.RefusalContract
import com.simprints.fingerprint.activities.refusal.RefusalPresenter
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManagerImpl
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelperImpl
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManagerImpl
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManagerImpl
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManagerImpl
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManagerImpl
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManagerImpl
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelperImpl
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.orchestrator.Orchestrator
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.factory.ScannerFactoryImpl
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.component.bluetooth.android.AndroidBluetoothAdapter
import com.simprints.id.Application
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicInteger

object KoinInjector {

    private val consumers = AtomicInteger(0)

    private var koinModule: Module? = null

    fun acquireFingerprintKoinModules() {
        consumers.incrementAndGet()
        if (koinModule == null) {
            val module = buildKoinModule()
            loadKoinModules(module)
            koinModule = module
        }
    }

    fun releaseFingerprintKoinModules() {
        if (consumers.decrementAndGet() == 0) {
            koinModule?.let {
                unloadKoinModules(it)
                koinModule = null
            }
        }
    }

    private fun buildKoinModule() =
        module(override = true) {
            defineBuildersForFingerprintManagers()
            defineBuildersForDomainClasses()
            defineBuildersForPresentersAndViewModels()
        }


    private fun Module.defineBuildersForFingerprintManagers() {
        factory<FingerprintPreferencesManager> { FingerprintPreferencesManagerImpl(get()) }
        factory<FingerprintAnalyticsManager> { FingerprintAnalyticsManagerImpl(get()) }
        factory<FingerprintSessionEventsManager> { FingerprintSessionEventsManagerImpl(get()) }
        factory<FingerprintCrashReportManager> { FingerprintCrashReportManagerImpl(get()) }
        factory<FingerprintTimeHelper> { FingerprintTimeHelperImpl(get()) }
        factory<FingerprintDbManager> { FingerprintDbManagerImpl(get()) }
        factory<FingerprintAndroidResourcesHelper> { FingerprintAndroidResourcesHelperImpl(get()) }
        factory<MasterFlowManager> { MasterFlowManagerImpl(get()) }
    }

    private fun Module.defineBuildersForDomainClasses() {
        single<BluetoothComponentAdapter> { AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter()) }
        single { ScannerUiHelper() }
        single<ScannerFactory> { ScannerFactoryImpl(get(), get()) }
        single<ScannerManager> { ScannerManagerImpl(get(), get()) }

        factory { FinalResultBuilder() }
        factory { Orchestrator(get()) }
    }

    private fun Module.defineBuildersForPresentersAndViewModels() {
        factory<AlertContract.Presenter> { (view: AlertContract.View, fingerprintAlert: FingerprintAlert) ->
            AlertPresenter(view, get(), get(), get(), fingerprintAlert)
        }
        factory<CollectFingerprintsContract.Presenter> { (context: Context, view: CollectFingerprintsContract.View, request: CollectFingerprintsTaskRequest) ->
            CollectFingerprintsPresenter(context, view, request, get(), get(), get(), get(), get(), get(), get())
        }
        factory<RefusalContract.Presenter> { (view: RefusalContract.View) ->
            RefusalPresenter(view, get(), get(), get())
        }

        viewModel { OrchestratorViewModel(get()) }
        viewModel { ConnectScannerViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { MatchingViewModel(get(), get(), get(), get(), get(), get()) }
    }

    private fun Scope.appComponent() =
        (androidApplication() as Application).component

    private fun Scope.orchestratorComponent() =
        (androidApplication() as Application).orchestratorComponent
}
