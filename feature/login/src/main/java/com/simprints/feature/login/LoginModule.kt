package com.simprints.feature.login

import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {
    @Provides
    fun provideGoogleApiAvailability() = GoogleApiAvailability.getInstance()
}
