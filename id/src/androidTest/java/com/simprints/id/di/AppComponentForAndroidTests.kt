package com.simprints.id.di

import com.simprints.id.coreFeatures.CollectFingerprintsActivityTest
import com.simprints.id.coreFeatures.HappyWorkflowAllMainFeatures
import com.simprints.id.data.analytics.eventData.SessionEventsManagerImplTest
import com.simprints.id.data.secure.SecureDataManagerTest
import com.simprints.id.secure.AuthTestsHappyWifi
import com.simprints.id.secure.AuthTestsNoWifi
import com.simprints.id.service.GuidSelectionServiceTest
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (PreferencesModule::class), (SerializerModule::class), (AndroidInjectionModule::class)])
interface AppComponentForAndroidTests : AppComponent {
    fun inject(happyWorkflowAllMainFeatures: HappyWorkflowAllMainFeatures)
    fun inject(secureDataManagerTest: SecureDataManagerTest)
    fun inject(authTestsHappyWifi: AuthTestsHappyWifi)
    fun inject(authTestsNoWifi: AuthTestsNoWifi)
    fun inject(sessionEventsManagerImplTest: SessionEventsManagerImplTest)
    fun inject(collectFingerprintsActivityTest: CollectFingerprintsActivityTest)
    fun inject(guidSelectionServiceTest: GuidSelectionServiceTest)
}
