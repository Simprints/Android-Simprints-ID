package com.simprints.fingerprint

import android.content.Context
import android.nfc.NfcAdapter
import com.simprints.fingerprint.activities.alert.AlertContract
import com.simprints.fingerprint.activities.alert.AlertPresenter
import com.simprints.fingerprint.activities.alert.FingerprintAlert
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
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManagerImpl
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelperImpl
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.factory.ScannerFactoryImpl
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.tools.nfc.android.AndroidNfcAdapter
import com.simprints.fingerprintmatcher.FingerprintMatcherModule
import com.simprints.fingerprintmatcher.JNILibAfisModule
import com.simprints.fingerprintscanner.FingerprintScannerModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.assisted.AssistedFactory
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(
    includes = [
        FingerprintDependenciesModule::class,
        FingerprintMatcherModule::class,
        JNILibAfisModule::class,
    ]
)
@InstallIn(SingletonComponent::class)
abstract class FingerprintModule {

    @AssistedFactory
    interface AlertPresenterFactory {
        fun create(
            view: AlertContract.View,
            alertType: FingerprintAlert,
        ): AlertPresenter
    }

    @AssistedFactory
    interface RefusalPresenterFactory {
        fun create(view: RefusalContract.View): RefusalPresenter
    }

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
    fun provideNfcAdapter(context: Context): ComponentNfcAdapter =
        AndroidNfcAdapter(NfcAdapter.getDefaultAdapter(context))
}
