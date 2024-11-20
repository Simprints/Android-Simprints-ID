package com.simprints.fingerprint.connect

import android.bluetooth.BluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.android.AndroidBluetoothAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScannerConnectModule {

    /*
    * This is the simulated implementation of the BluetoothAdapter
    *  uncomment this and the import of the scanner mock module in the build.gradle.kts
     */
/*
    @Provides
    @Singleton
    fun provideComponentBluetoothAdapter(@ApplicationContext ctx: Context): ComponentBluetoothAdapter =
        SimulatedBluetoothAdapter(SimulatedScannerManager(
            simulationMode = SimulationMode.V2,
            isAdapterNull = false,
            isAdapterEnabled = true,
            isDeviceBonded = true,
            deviceName = "Simulated Scanner",
            outgoingStreamObservers = setOf(),
            context = ctx,
        ))
*/

    /*
    * This is the real implementation of the BluetoothAdapter
     */
    @Provides
    @Singleton
    fun provideComponentBluetoothAdapter(): ComponentBluetoothAdapter =
        AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())
}
