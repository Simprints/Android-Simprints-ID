package com.simprints.feature.login

import com.google.android.gms.common.GoogleApiAvailability
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
    fun provideGoogleApiAvailability() = GoogleApiAvailability.getInstance()
}
