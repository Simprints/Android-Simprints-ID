package com.simprints.fingerprintscanner

import android.bluetooth.BluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.android.AndroidBluetoothAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FingerprintScannerModule {

    @Provides
    @Singleton
    fun provideComponentBluetoothAdapter(): ComponentBluetoothAdapter =
        AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())
}
