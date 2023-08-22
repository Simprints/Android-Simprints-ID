package com.simprints.fingerprint

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.nfc.NfcAdapter
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManagerImpl
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManagerImpl
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManagerImpl
import com.simprints.fingerprint.controllers.core.network.FingerprintApiClientFactory
import com.simprints.fingerprint.controllers.core.network.FingerprintApiClientFactoryImpl
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManagerImpl
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelperImpl
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.android.AndroidBluetoothAdapter
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.factory.ScannerFactoryImpl
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.tools.nfc.android.AndroidNfcAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(
    includes = [
        FingerprintDependenciesModule::class,
    ]
)
@InstallIn(SingletonComponent::class)
abstract class FingerprintModule {

    @Binds
    abstract fun provideMasterFlowManager(impl: MasterFlowManagerImpl): MasterFlowManager

    @Binds
    abstract fun provideScannerManager(impl: ScannerManagerImpl): ScannerManager

    @Binds
    abstract fun provideScannerFactory(impl: ScannerFactoryImpl): ScannerFactory

    @Binds
    abstract fun provideFingerprintImageManager(impl: FingerprintImageManagerImpl): FingerprintImageManager

    @Binds
    abstract fun provideFingerprintTimeHelper(impl: FingerprintTimeHelperImpl): FingerprintTimeHelper

    @Binds
    abstract fun provideFingerprintSessionEventsManager(impl: FingerprintSessionEventsManagerImpl): FingerprintSessionEventsManager

    @Binds
    abstract fun provideFingerprintDbManager(impl: FingerprintDbManagerImpl): FingerprintDbManager

    @Binds
    abstract fun provideFingerprintApiClientFactory(impl: FingerprintApiClientFactoryImpl): FingerprintApiClientFactory
}

@Module
@InstallIn(SingletonComponent::class)
object FingerprintDependenciesModule {

    @Provides
    fun provideNfcAdapter(@ApplicationContext context: Context): ComponentNfcAdapter =
        AndroidNfcAdapter(NfcAdapter.getDefaultAdapter(context))

    /**
     * To provide alternative implementation for BT adapter replace
     * returned instance with any of the mock implementations:
     *  - DummyBluetoothAdapter()
     *  - AndroidRecordBluetoothAdapter()
     *  - SimulatedBluetoothAdapter(SimulatedScannerManager(...))
     *
     *  Also change the dependency declaration in build.gradle.kts
     */
    @Provides
    @Singleton
    fun provideComponentBluetoothAdapter(): ComponentBluetoothAdapter =
        AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())
}
