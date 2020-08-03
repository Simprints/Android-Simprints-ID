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
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.verify
import junit.framework.Assert.assertEquals
import org.junit.Assert
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
        Assert.assertEquals(SettingsPreferencesManagerImpl.LOGO_EXISTS_DEFAULT, originalValue)

        settingsPreferencesManager.logoExists = !originalValue

        val newValue = settingsPreferencesManager.logoExists

        Assert.assertEquals(originalValue, newValue)

        verify(exactly = 2) { remoteConfigSpy.getBoolean(SettingsPreferencesManagerImpl.LOGO_EXISTS_KEY) }
    }

    @Test
    fun fetchingOverridableRemoteConfigPrimitive_worksAndBecomesOverridden() {
        val originalValue = settingsPreferencesManager.language
        Assert.assertEquals(SHARED_PREFS_LANGUAGE_DEFAULT, originalValue)

        settingsPreferencesManager.language = SHARED_PREFS_LANGUAGE_DEFAULT + "q"

        val newValue = settingsPreferencesManager.language

        Assert.assertNotEquals(originalValue, newValue)

        verify(exactly = 1) { remoteConfigSpy.getString(SHARED_PREFS_LANGUAGE_KEY) }
    }

    @Test
    fun fetchingRemoteConfigEnum_worksAndDoesNotGetOverridden() {
        val oldMatchGroup = settingsPreferencesManager.matchGroup
        Assert.assertEquals(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT, oldMatchGroup)

        settingsPreferencesManager.matchGroup = GROUP.MODULE

        val newMatchGroup = settingsPreferencesManager.matchGroup

        Assert.assertEquals(oldMatchGroup, newMatchGroup)

        verify(exactly = 2) { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) }
    }

    @Test
    fun fetchingRemoteConfigEnum_revertsToDefaultIfSetToUnrecognizedValue() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) } returns "PROJECT"

        val matchGroup = settingsPreferencesManager.matchGroup

        Assert.assertEquals(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT, matchGroup)
    }

    @Test
    fun fetchingRemoteConfigEnum_revertsToDefaultIfSetToWrongType() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) } returns "1"

        val matchGroup = settingsPreferencesManager.matchGroup

        assertEquals(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT, matchGroup)
    }

    @Test
    fun fetchingOverridableRemoteConfigFingerIdMap_worksAndBecomesOverridden() {
        val oldFingerStatus = settingsPreferencesManager.fingerStatus
        Assert.assertEquals(SettingsPreferencesManagerImpl.FINGER_STATUS_DEFAULT, oldFingerStatus)

        settingsPreferencesManager.fingerStatus = NON_DEFAULT_FINGER_STATUS_TARGET

        val newFingerStatus = settingsPreferencesManager.fingerStatus

        Assert.assertEquals(NON_DEFAULT_FINGER_STATUS_TARGET, newFingerStatus)

        verify(exactly = 1) { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGER_STATUS_KEY) }
    }

    @Test
    fun fetchingOverridableRemoteConfigFingerIdMap_worksForNonDefaultValue() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGER_STATUS_KEY) } returns NON_DEFAULT_FINGER_STATUS_SERIALIZED

        val fingerStatus = settingsPreferencesManager.fingerStatus

        Assert.assertEquals(NON_DEFAULT_FINGER_STATUS_TARGET, fingerStatus)
    }

    @Test
    fun fetchingOverridableRemoteConfigFingerIdMap_throwsIfMalformed() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGER_STATUS_KEY) } returns MALFORMED_FINGER_STATUS_SERIALIZED

        shouldThrow<MismatchedInputException> {
            settingsPreferencesManager.fingerStatus
        }
    }

    companion object {

        private const val NON_DEFAULT_FINGER_STATUS_SERIALIZED = "{\"RIGHT_5TH_FINGER\":\"true\",\"RIGHT_4TH_FINGER\":\"true\",\"RIGHT_3RD_FINGER\":\"false\",\"RIGHT_INDEX_FINGER\":\"false\",\"RIGHT_THUMB\":\"false\",\"LEFT_THUMB\":\"true\",\"LEFT_INDEX_FINGER\":\"false\",\"LEFT_3RD_FINGER\":\"false\",\"LEFT_4TH_FINGER\":\"false\",\"LEFT_5TH_FINGER\":\"true\"}"
        private val NON_DEFAULT_FINGER_STATUS_TARGET = mapOf(
            FingerIdentifier.RIGHT_THUMB to false,
            FingerIdentifier.RIGHT_INDEX_FINGER to false,
            FingerIdentifier.RIGHT_3RD_FINGER to false,
            FingerIdentifier.RIGHT_4TH_FINGER to true,
            FingerIdentifier.RIGHT_5TH_FINGER to true,
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to false,
            FingerIdentifier.LEFT_3RD_FINGER to false,
            FingerIdentifier.LEFT_4TH_FINGER to false,
            FingerIdentifier.LEFT_5TH_FINGER to true
        )

        private const val MALFORMED_FINGER_STATUS_SERIALIZED = "\"gibberish{\\\"000}\\\"\\\"\""
    }
}
