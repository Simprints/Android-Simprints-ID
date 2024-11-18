package com.simprints.fingerprint.infra.scanner

import android.content.Context
import android.nfc.NfcAdapter
import com.simprints.fingerprint.infra.scanner.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.infra.scanner.nfc.android.AndroidNfcAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module(
    includes = [
        FingerprintDependenciesModule::class,
    ]
)
@InstallIn(SingletonComponent::class)
abstract class ScannerModule {

    @Binds
    internal abstract fun provideScannerManager(impl: ScannerManagerImpl): ScannerManager

}

@Module
@InstallIn(SingletonComponent::class)
object FingerprintDependenciesModule {

    @Provides
    fun provideNfcAdapter(@ApplicationContext context: Context): ComponentNfcAdapter =
        AndroidNfcAdapter(NfcAdapter.getDefaultAdapter(context))
}
