package com.simprints.id.data.prefs

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.core.tools.utils.LanguageHelper.SHARED_PREFS_LANGUAGE_DEFAULT
import com.simprints.core.tools.utils.LanguageHelper.SHARED_PREFS_LANGUAGE_KEY
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.GROUP
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.every
import io.mockk.verify
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SettingsPreferencesManagerTest {

    @Inject lateinit var remoteConfigSpy: FirebaseRemoteConfig
    @Inject lateinit var settingsPreferencesManager: SettingsPreferencesManager

    private val preferencesModule by lazy {
        TestPreferencesModule(remoteConfigRule = DependencyRule.SpykRule)
    }

    @Before
    fun setup() {
        UnitTestConfig(this, null, preferencesModule).fullSetup()

        every { remoteConfigSpy.getBoolean(RemoteConfigWrapper.PROJECT_SPECIFIC_MODE_KEY) } returns true
    }

    @Test
    fun fetchingRemoteConfigPrimitive_worksAndDoesNotGetOverridden() {
        val originalValue = settingsPreferencesManager.logoExists
        assertEquals(SettingsPreferencesManagerImpl.LOGO_EXISTS_DEFAULT, originalValue)

        settingsPreferencesManager.logoExists = !originalValue

        val newValue = settingsPreferencesManager.logoExists

        assertEquals(originalValue, newValue)

        verify(exactly = 2) { remoteConfigSpy.getBoolean(SettingsPreferencesManagerImpl.LOGO_EXISTS_KEY) }
    }

    @Test
    fun fetchingOverridableRemoteConfigPrimitive_worksAndBecomesOverridden() {
        val originalValue = settingsPreferencesManager.language
        assertEquals(SHARED_PREFS_LANGUAGE_DEFAULT, originalValue)

        settingsPreferencesManager.language = SHARED_PREFS_LANGUAGE_DEFAULT + "q"

        val newValue = settingsPreferencesManager.language

        Assert.assertNotEquals(originalValue, newValue)

        verify(exactly = 1) { remoteConfigSpy.getString(SHARED_PREFS_LANGUAGE_KEY) }
    }

    @Test
    fun fetchingRemoteConfigEnum_worksAndDoesNotGetOverridden() {
        val oldMatchGroup = settingsPreferencesManager.matchGroup
        assertEquals(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT, oldMatchGroup)

        settingsPreferencesManager.matchGroup = GROUP.MODULE

        val newMatchGroup = settingsPreferencesManager.matchGroup

        assertEquals(oldMatchGroup, newMatchGroup)

        verify(exactly = 2) { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) }
    }

    @Test
    fun fetchingRemoteConfigEnum_revertsToDefaultIfSetToUnrecognizedValue() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) } returns "PROJECT"

        val matchGroup = settingsPreferencesManager.matchGroup

        assertEquals(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT, matchGroup)
    }

    @Test
    fun fetchingRemoteConfigEnum_revertsToDefaultIfSetToWrongType() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) } returns "1"

        val matchGroup = settingsPreferencesManager.matchGroup

        assertEquals(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT, matchGroup)
    }

    @Test
    fun fetchingOverridableRemoteConfigFingersToCollect_worksAndBecomesOverridden() {
        val oldFingersToCollect = settingsPreferencesManager.fingerprintsToCollect
        assertEquals(SettingsPreferencesManagerImpl.FINGERPRINTS_TO_COLLECT_DEFAULT, oldFingersToCollect)

        settingsPreferencesManager.fingerprintsToCollect = NON_DEFAULT_FINGERS_TO_COLLECT

        val newFingerStatus = settingsPreferencesManager.fingerprintsToCollect

        assertEquals(NON_DEFAULT_FINGERS_TO_COLLECT, newFingerStatus)

        verify(exactly = 1) { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGERPRINTS_TO_COLLECT_KEY) }
    }

    @Test
    fun fetchingOverridableRemoteConfigFingerIdMap_worksForNonDefaultValue() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGERPRINTS_TO_COLLECT_KEY) } returns NON_DEFAULT_FINGERS_TO_COLLECT_SERIALIZED

        val fingerStatus = settingsPreferencesManager.fingerprintsToCollect

        assertEquals(NON_DEFAULT_FINGERS_TO_COLLECT, fingerStatus)
    }

    companion object {

        private const val NON_DEFAULT_FINGERS_TO_COLLECT_SERIALIZED = "RIGHT_4TH_FINGER,RIGHT_5TH_FINGER,LEFT_THUMB,LEFT_5TH_FINGER"
        private val NON_DEFAULT_FINGERS_TO_COLLECT = listOf(
            FingerIdentifier.RIGHT_4TH_FINGER,
            FingerIdentifier.RIGHT_5TH_FINGER,
            FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.LEFT_5TH_FINGER
        )
    }
}
