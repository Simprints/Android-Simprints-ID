package com.simprints.id.di

import com.simprints.id.data.secure.SecureDataManagerTest
import com.simprints.id.secure.AuthTestsHappyWifi
import com.simprints.id.secure.AuthTestsNoWifi
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (PreferencesModule::class), (SerializerModule::class), (AndroidInjectionModule::class)])
interface AppComponentForAndroidTests : AppComponent {
    fun inject(secureDataManagerTest: SecureDataManagerTest)
    fun inject(authTestsHappyWifi: AuthTestsHappyWifi)
    fun inject(authTestsNoWifi: AuthTestsNoWifi)

}
