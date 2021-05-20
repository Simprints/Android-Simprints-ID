package com.simprints.fingerprint.di

import android.bluetooth.BluetoothAdapter
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modality
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.sharedpreferences.RecentEventsPreferencesManager
import com.simprints.core.tools.constants.SharedPrefsConstants
import com.simprints.core.tools.json.JsonHelper
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
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManagerImpl
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManagerImpl
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
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.IdPreferencesManagerImpl
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferencesImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.data.prefs.settings.fingerprint.serializers.FingerprintsToCollectSerializer
import com.simprints.id.data.prefs.settings.fingerprint.serializers.ScannerGenerationsSerializer
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting
import com.simprints.id.tools.serializers.*
import org.koin.android.ext.koin.androidApplication
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
        module(override = true) {
            defineBuildersForFingerprintManagers()
            defineBuildersForDomainClasses()
            defineBuildersForPresentersAndViewModels()
            definePreferencesManager()
        }


    /**
     * These are classes that are wrappers of ones that appear in the main app module
     */
    private fun Module.defineBuildersForFingerprintManagers() {
        factory<FingerprintAnalyticsManager> { FingerprintAnalyticsManagerImpl(get()) }
        factory<FingerprintSessionEventsManager> { FingerprintSessionEventsManagerImpl(get()) }
        factory<FingerprintCrashReportManager> { FingerprintCrashReportManagerImpl(get()) }
        factory<FingerprintTimeHelper> { FingerprintTimeHelperImpl(get()) }
        factory<FingerprintDbManager> { FingerprintDbManagerImpl(get()) }
        factory<MasterFlowManager> { MasterFlowManagerImpl(get()) }
        factory<FingerprintImageManager> { FingerprintImageManagerImpl(get(), get()) }
        factory<FingerprintApiClientFactory> { FingerprintApiClientFactoryImpl(get()) }
    }

    private fun Module.defineBuildersForDomainClasses() {
        factory { SerialNumberConverter() }
        factory { ScannerGenerationDeterminer() }
        factory { FingerprintFileDownloader() }

        factory { BatteryLevelChecker(androidContext()) }
        factory { FirmwareLocalDataSource(androidContext()) }
        factory { FirmwareRemoteDataSource(get(), get()) }
        factory { FirmwareRepository(get(), get()) }
        factory { FirmwareFileUpdateScheduler(androidContext(), get()) }

        single<ComponentBluetoothAdapter> { AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter()) }
        single { ScannerUiHelper() }
        single { ScannerPairingManager(get(), get(), get(), get()) }
        single { ScannerInitialSetupHelper(get(), get(), get()) }
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
            AlertPresenter(view, get(), get(), get(), fingerprintAlert)
        }
        factory<RefusalContract.Presenter> { (view: RefusalContract.View) ->
            RefusalPresenter(view, get(), get(), get())
        }

        viewModel { OrchestratorViewModel(get(), get(), get(), get()) }
        viewModel { ConnectScannerViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel {
            CollectFingerprintsViewModel(
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
        viewModel { MatchingViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { NfcPairViewModel(get(), get()) }
        viewModel { SerialEntryPairViewModel(get(), get()) }
        viewModel { OtaViewModel(get(), get(), get(), get(), get()) }
        viewModel { OtaRecoveryViewModel(get()) }
    }

    private fun Module.definePreferencesManager() {
        single<IdPreferencesManager> { IdPreferencesManagerImpl(get(), get(), get()) }
        single<SettingsPreferencesManager> {
            SettingsPreferencesManagerImpl(
                get(),
                get(),
                get(named("GroupSerializer")),
                get(named("ModalitiesSerializer")),
                get(named("LanguagesStringArraySerializer")),
                get(named("ModuleIdOptionsStringSetSerializer")),
                get(named("PeopleDownSyncSettingSerializer")),
                get(named("CaptureFingerprintStrategySerializer")),
                get(named("SaveFingerprintImagesStrategySerializer")),
                get(named("ScannerGenerationsSerializer")),
                get(named("FingerprintsToCollectSerializer")),
                get(named("FingerprintConfidenceThresholdsSerializer")),
                get(named("FaceConfidenceThresholdsSerializer")),
                get(named("SyncDestinationSerializer"))
            )
        }
        factory<Serializer<GROUP>>(named("GroupSerializer")) { EnumSerializer(GROUP::class.java) }
        factory<Serializer<Array<String>>>(named("LanguagesStringArraySerializer")) { LanguagesStringArraySerializer() }
        factory<Serializer<Set<String>>>(named("ModuleIdOptionsStringSetSerializer")) { ModuleIdOptionsStringSetSerializer() }
        factory<Serializer<EventDownSyncSetting>>(named("PeopleDownSyncSettingSerializer")) {
            EnumSerializer(
                EventDownSyncSetting::class.java
            )
        }
        factory<Serializer<List<SyncDestinationSetting>>>(named("SyncDestinationSerializer")) { SyncDestinationListSerializer() }
        factory<Serializer<List<Modality>>>(named("ModalitiesSerializer")) { ModalitiesListSerializer() }
        factory<Serializer<CaptureFingerprintStrategy>>(named("CaptureFingerprintStrategySerializer")) {
            EnumSerializer(
                CaptureFingerprintStrategy::class.java
            )
        }
        factory<Serializer<SaveFingerprintImagesStrategy>>(named("SaveFingerprintImagesStrategySerializer")) {
            EnumSerializer(
                SaveFingerprintImagesStrategy::class.java
            )
        }
        factory<Serializer<List<ScannerGeneration>>>(named("ScannerGenerationsSerializer")) { ScannerGenerationsSerializer() }
        factory<Serializer<List<FingerIdentifier>>>(named("FingerprintsToCollectSerializer")) { FingerprintsToCollectSerializer() }
        factory<Serializer<Map<FingerprintConfidenceThresholds, Int>>>(named("FingerprintConfidenceThresholdsSerializer")) {
            MapSerializer(
                get(named("FingerprintConfidenceSerializer")),
                get(named("IntSerializer")),
                get()
            )
        }
        factory<Serializer<Map<FaceConfidenceThresholds, Int>>>(named("FaceConfidenceThresholdsSerializer")) {
            MapSerializer(
                get(named("FaceConfidenceSerializer")),
                get(named("IntSerializer")),
                get()
            )
        }
        factory<Serializer<FingerprintConfidenceThresholds>>(named("FingerprintConfidenceSerializer")) {
            EnumSerializer(
                FingerprintConfidenceThresholds::class.java
            )
        }
        factory<Serializer<FaceConfidenceThresholds>>(named("FaceConfidenceSerializer")) {
            EnumSerializer(
                FaceConfidenceThresholds::class.java
            )
        }
        factory<Serializer<Int>>(named("IntSerializer")) { IntegerSerializer() }
        factory { JsonHelper }
        single<RecentEventsPreferencesManager> { RecentEventsPreferencesManagerImpl(get()) }
        single<ImprovedSharedPreferences> { ImprovedSharedPreferencesImpl(get()) }
        factory<SharedPreferences> {
            androidApplication().getSharedPreferences(
                SharedPrefsConstants.PREF_FILE_NAME,
                SharedPrefsConstants.PREF_MODE
            )
        }
        single<FingerprintPreferencesManager> { FingerprintPreferencesManagerImpl(get()) }
    }

}
