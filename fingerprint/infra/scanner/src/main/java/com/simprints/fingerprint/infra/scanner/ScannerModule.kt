package com.simprints.fingerprint.infra.scanner

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.nfc.NfcAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.android.AndroidBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.infra.scanner.nfc.android.AndroidNfcAdapter
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
abstract class ScannerModule {

    @Binds
    abstract fun provideScannerManager(impl: ScannerManagerImpl): ScannerManager

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
