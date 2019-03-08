package com.simprints.fingerprints.di

import com.simprints.fingerprints.activities.matching.MatchingActivity
import com.simprints.fingerprints.activities.matching.MatchingPresenter
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
