package com.simprints.id.data.prefs

import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockito_kotlin.times
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.DaggerForTests
import com.simprints.id.shared.DependencyRule.SpyRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.anyNotNull
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class SettingsPreferencesManagerTest : DaggerForTests() {

    @Inject lateinit var remoteConfigSpy: FirebaseRemoteConfig
    @Inject lateinit var settingsPreferencesManager: SettingsPreferencesManager

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(remoteConfigRule = SpyRule)
    }

    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)
    }

    @Test
    fun fetchingRemoteConfigPrimitive_worksAndDoesNotGetOverridden() {
        val originalValue = settingsPreferencesManager.parentalConsentExists

        settingsPreferencesManager.parentalConsentExists = !originalValue

        val newValue = settingsPreferencesManager.parentalConsentExists

        Assert.assertEquals(originalValue, newValue)

        verify(remoteConfigSpy, times(2)).getBoolean(anyNotNull())
    }

    @Test
    fun fetchingOverridableRemoteConfigPrimitive_worksAndBecomesOverridden() {
        val originalValue = settingsPreferencesManager.syncOnCallout

        settingsPreferencesManager.syncOnCallout = !originalValue

        val newValue = settingsPreferencesManager.syncOnCallout

        Assert.assertEquals(originalValue, !newValue)

        verify(remoteConfigSpy, times(1)).getBoolean(anyNotNull())
    }
}
