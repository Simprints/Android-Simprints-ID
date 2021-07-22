package com.simprints.fingerprint.di

import android.bluetooth.BluetoothAdapter
import android.nfc.NfcAdapter
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.fingerprint.activities.alert.AlertContract
import com.simprints.fingerprint.activities.alert.AlertPresenter
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.activities.collect.domain.FingerPriorityDeterminer
import com.simprints.fingerprint.activities.collect.domain.StartingStateDeterminer
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.nfcpair.NfcPairViewModel
import com.simprints.fingerprint.activities.connect.issues.ota.OtaViewModel
import com.simprints.fingerprint.activities.connect.issues.otarecovery.OtaRecoveryViewModel
import com.simprints.fingerprint.activities.connect.issues.serialentrypair.SerialEntryPairViewModel
import com.simprints.fingerprint.activities.matching.MatchingViewModel
import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel
import com.simprints.fingerprint.activities.refusal.RefusalContract
import com.simprints.fingerprint.activities.refusal.RefusalPresenter
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManagerImpl
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManagerImpl
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManagerImpl
import com.simprints.fingerprint.controllers.core.network.FingerprintApiClientFactory
import com.simprints.fingerprint.controllers.core.network.FingerprintApiClientFactoryImpl
import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManagerImpl
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManagerImpl
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelperImpl
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationController
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.orchestrator.Orchestrator
import com.simprints.fingerprint.orchestrator.runnable.RunnableTaskDispatcher
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.data.FirmwareRepository
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.data.remote.FirmwareRemoteDataSource
import com.simprints.fingerprint.scanner.data.worker.FirmwareFileUpdateScheduler
import com.simprints.fingerprint.scanner.domain.versions.ScannerHardwareRevisionsSerializer
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.factory.ScannerFactoryImpl
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprint.tools.BatteryLevelChecker
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.tools.nfc.android.AndroidNfcAdapter
import com.simprints.fingerprintmatcher.FingerprintMatcher
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.android.AndroidBluetoothAdapter
import com.simprints.id.Application
import com.simprints.id.di.AppComponent
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicInteger

/**
 * Note on the consumers mechanism - consider this flow:
 * - consumers starts at 0
 * - Setup callout: OrchestratorActivity.onCreate() , Koin modules created, consumers + 1 → 1 (The Koin modules are now loaded)
 * - Then, Normal callout (e.g. match) begins before previous on destroy: OrchestratorActivity.onCreate(), consumers + 1 → 2
 * - Setup callout now destroys shortly after like you described: OrchestratorActivity.onDestroy(), consumers - 1 → 1 (Koin modules still loaded)
 * - Normal callout finishes as usual after the work is done: OrchestratorActivity.onDestroy(), consumers - 1 → 0 (the Koin modules are now unloaded)
 *
 * This means we don’t need to worry about this mis-ordering during the flow - the unload only happens when the counter reaches 0.
 * If you call `acquire…` and `release…` every activity, this is additionally helpful for situations where the Orchestrator
 * activity is destroyed while it’s in the background because of low memory.
 */
object KoinInjector {

    private val consumers = AtomicInteger(0)

    private var koinModule: Module? = null

    /**
     * Call this on the first point of contact of your modality (usually onCreate() of OrchestratorActivity)
     */
    fun acquireFingerprintKoinModules() {
        consumers.incrementAndGet()
        if (koinModule == null) {
            val module = buildKoinModule()
            loadKoinModules(module)
            koinModule = module
        }
    }

    /**
     * Call this on the last point of contact of your modality, usually onDestroy()
     */
    fun releaseFingerprintKoinModules() {
        if (consumers.decrementAndGet() == 0) {
            koinModule?.let {
                unloadKoinModules(it)
                koinModule = null
            }
        }
    }

    private fun buildKoinModule() =
        module {
            defineBuildersForFingerprintManagers()
            defineBuildersForDomainClasses()
            defineBuildersForPresentersAndViewModels()
        }

    /**
     * These are classes that are wrappers of ones that appear in the main app module
     */
    private fun Module.defineBuildersForFingerprintManagers() {
        single { ScannerHardwareRevisionsSerializer(JsonHelper) }
        single<FingerprintPreferencesManager> { FingerprintPreferencesManagerImpl(get(), get()) }
        factory<FingerprintSessionEventsManager> { FingerprintSessionEventsManagerImpl(get()) }
        factory<FingerprintTimeHelper> { FingerprintTimeHelperImpl(get()) }
        factory<FingerprintDbManager> { FingerprintDbManagerImpl(get()) }
        factory<MasterFlowManager> { MasterFlowManagerImpl(get()) }
        factory<FingerprintImageManager> { FingerprintImageManagerImpl(get(), get()) }
        factory<FingerprintApiClientFactory> { FingerprintApiClientFactoryImpl(get()) }
        factory<DispatcherProvider> { DefaultDispatcherProvider() }
    }

    private fun Module.defineBuildersForDomainClasses() {
        factory { SerialNumberConverter() }
        factory { ScannerGenerationDeterminer() }

        factory { BatteryLevelChecker(androidContext()) }
        factory { FirmwareLocalDataSource(androidContext()) }
        factory { androidContext().applicationContext as Application }

        factory { get<Application>().component }
        factory { get<AppComponent>().getLoginManager() }
        factory<DispatcherProvider> { DefaultDispatcherProvider() }
        factory { FingerprintFileDownloader(get(), get(), get()) }
        factory { FirmwareRemoteDataSource(get(), get()) }
        factory { FirmwareRepository(get(), get(), get()) }
        factory { FirmwareFileUpdateScheduler(androidContext(), get()) }

        single<ComponentBluetoothAdapter> { AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter()) }
        single { ScannerUiHelper() }
        single { ScannerPairingManager(get(), get(), get(), get()) }
        single { ScannerInitialSetupHelper(get(), get(), get(), get()) }
        single { ConnectionHelper(get()) }
        single { CypressOtaHelper(get(), get()) }
        single { StmOtaHelper(get(), get()) }
        single { Un20OtaHelper(get(), get()) }
        single<ScannerFactory> {
            ScannerFactoryImpl(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }
        single<ScannerManager> { ScannerManagerImpl(get(), get(), get(), get()) }

        single<ComponentNfcAdapter> { AndroidNfcAdapter(NfcAdapter.getDefaultAdapter(get())) }
        single { NfcManager(get()) }

        factory { ConfigurationController() }
        factory { RunnableTaskDispatcher(get()) }
        factory { FinalResultBuilder() }
        factory { Orchestrator(get()) }

        factory { FingerPriorityDeterminer() }
        factory { StartingStateDeterminer() }

        factory { FingerprintMatcher.create() }
    }

    private fun Module.defineBuildersForPresentersAndViewModels() {
        factory<AlertContract.Presenter> { (view: AlertContract.View, fingerprintAlert: FingerprintAlert) ->
            AlertPresenter(view, get(), get(), fingerprintAlert)
        }
        factory<RefusalContract.Presenter> { (view: RefusalContract.View) ->
            RefusalPresenter(view, get(), get())
        }
        single<EncodingUtils> { EncodingUtilsImpl }

        viewModel {
            OrchestratorViewModel(
                get(),
                get(),
                get(),
                get(),
                get(qualifier = named(Application.APPLICATION_SCOPE))
            )
        }
        viewModel { ConnectScannerViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel {
            CollectFingerprintsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(qualifier = named(Application.APPLICATION_SCOPE)),
                get()
            )
        }
        viewModel { MatchingViewModel(get(), get(), get(), get(), get(),get(),get()) }
        viewModel { NfcPairViewModel(get(), get()) }
        viewModel { SerialEntryPairViewModel(get(), get()) }
        viewModel { OtaViewModel(get(), get(), get(), get(), get()) }
        viewModel { OtaRecoveryViewModel(get()) }
    }

}
