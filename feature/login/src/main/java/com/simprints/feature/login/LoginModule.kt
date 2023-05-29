package com.simprints.feature.login

import com.simprints.core.DispatcherBG
import com.simprints.feature.login.tools.camera.QrCodeAnalyzer
import com.simprints.feature.login.tools.camera.QrCodeDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher


@Module
@InstallIn(SingletonComponent::class)
object LoginModule {

    @Provides
    internal fun provideQrCodeAnalyzer(
        qrCodeDetector: QrCodeDetector,
        @DispatcherBG coroutineDispatcher: CoroutineDispatcher,
    ) = QrCodeAnalyzer(qrCodeDetector, coroutineDispatcher)

}
