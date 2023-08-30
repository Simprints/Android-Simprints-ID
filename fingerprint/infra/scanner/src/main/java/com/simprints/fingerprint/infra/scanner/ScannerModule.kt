package com.simprints.fingerprint.infra.scanner

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    @Provides
    @Singleton
    fun provideFingerprintCaptureWrapperFactory(
        @DispatcherIO dispatcherIO: CoroutineDispatcher,
        scannerUiHelper: ScannerUiHelper
    ): FingerprintCaptureWrapperFactory {
        return FingerprintCaptureWrapperFactory(dispatcherIO, scannerUiHelper)
    }

}




