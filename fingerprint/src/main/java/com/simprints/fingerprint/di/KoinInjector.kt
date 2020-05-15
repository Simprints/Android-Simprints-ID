package com.simprints.fingerprint.di

import android.bluetooth.BluetoothAdapter
import android.nfc.NfcAdapter
import com.simprints.fingerprint.activities.alert.AlertContract
import com.simprints.fingerprint.activities.alert.AlertPresenter
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.activities.collect.domain.FingerOrderDeterminer
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.nfcpair.NfcPairViewModel
import com.simprints.fingerprint.activities.connect.issues.serialentrypair.SerialEntryPairViewModel
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
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManagerImpl
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManagerImpl
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManagerImpl
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelperImpl
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.orchestrator.Orchestrator
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.controllers.v2.ConnectionHelper
import com.simprints.fingerprint.scanner.controllers.v2.ScannerInitialSetupHelper
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.factory.ScannerFactoryImpl
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.tools.nfc.android.AndroidNfcAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.android.AndroidBluetoothAdapter
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
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
        factory<FingerprintImageManager> { FingerprintImageManagerImpl(get(), get()) }
    }

    private fun Module.defineBuildersForDomainClasses() {
        factory { SerialNumberConverter() }
        factory { ScannerGenerationDeterminer() }

        single<ComponentBluetoothAdapter> { AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter()) }
        single { ScannerUiHelper() }
        single { ScannerPairingManager(get()) }
        single { ScannerInitialSetupHelper() }
        single { ConnectionHelper(get()) }
        single<ScannerFactory> { ScannerFactoryImpl(get(), get(), get(), get(), get(), get(), get(), get()) }
        single<ScannerManager> { ScannerManagerImpl(get(), get(), get(), get()) }

        single<ComponentNfcAdapter> { AndroidNfcAdapter(NfcAdapter.getDefaultAdapter(get())) }
        single { NfcManager(get()) }

        factory { FinalResultBuilder() }
        factory { Orchestrator(get()) }

        factory { FingerOrderDeterminer() }
    }

    private fun Module.defineBuildersForPresentersAndViewModels() {
        factory<AlertContract.Presenter> { (view: AlertContract.View, fingerprintAlert: FingerprintAlert) ->
            AlertPresenter(view, get(), get(), get(), fingerprintAlert)
        }
        factory<RefusalContract.Presenter> { (view: RefusalContract.View) ->
            RefusalPresenter(view, get(), get(), get())
        }

        viewModel { OrchestratorViewModel(get(), get()) }
        viewModel { ConnectScannerViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { CollectFingerprintsViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { MatchingViewModel(get(), get(), get(), get(), get()) }
        viewModel { NfcPairViewModel(get(), get()) }
        viewModel { SerialEntryPairViewModel(get(), get()) }
    }
}
