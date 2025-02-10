package com.simprints.fingerprint.infra.scanner

import android.content.Context
import android.nfc.NfcAdapter
import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.infra.scanner.nfc.android.AndroidNfcAdapter
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.ByteArrayToPacketAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.PacketParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.PacketRouter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Suppress("unused")
@Module(
    includes = [
        FingerprintDependenciesModule::class,
    ],
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
    fun provideNfcAdapter(
        @ApplicationContext context: Context,
    ): ComponentNfcAdapter = AndroidNfcAdapter(NfcAdapter.getDefaultAdapter(context))

    @Provides
    fun providePacketRouter(
        @DispatcherIO ioDispatcher: CoroutineDispatcher,
    ): PacketRouter = PacketRouter(
        listOf(Route.Remote.VeroServer, Route.Remote.VeroEvent, Route.Remote.Un20Server),
        { source },
        ByteArrayToPacketAccumulator(PacketParser()),
        ioDispatcher,
    )
}
