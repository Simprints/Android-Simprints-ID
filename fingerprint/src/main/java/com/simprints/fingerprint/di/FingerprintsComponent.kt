package com.simprints.fingerprint.di

import com.simprints.fingerprint.activities.matching.MatchingActivity
import com.simprints.fingerprint.activities.matching.MatchingPresenter
import com.simprints.id.di.AppModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SerializerModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, SerializerModule::class, PreferencesModule::class])
interface FingerprintsComponent {

    fun inject(matchingActivity: MatchingActivity)
    fun inject(matchingPresenter: MatchingPresenter)
}
