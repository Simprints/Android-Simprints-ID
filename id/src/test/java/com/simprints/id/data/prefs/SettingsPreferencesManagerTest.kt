package com.simprints.id.data.prefs

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.JsonSyntaxException
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.commontesttools.di.DependencyRule.SpyRule
import com.simprints.id.commontesttools.di.PreferencesModuleForAnyTests
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.Constants
import com.simprints.id.testtools.roboletric.RobolectricDaggerTestConfig
import com.simprints.id.testtools.roboletric.TestApplication
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.testframework.common.syntax.assertThrows
import com.simprints.testframework.common.syntax.verifyExactly
import com.simprints.testframework.common.syntax.verifyOnce
import com.simprints.testframework.common.syntax.whenever
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
        PreferencesModuleForAnyTests(remoteConfigRule = SpyRule)
    }

    @Before
    fun setup() {
        RobolectricDaggerTestConfig(this, null, preferencesModule).setupFirebase().finish()

        whenever { remoteConfigSpy.getBoolean(RemoteConfigWrapper.PROJECT_SPECIFIC_MODE_KEY) } thenReturn true
    }

    @Test
    fun fetchingRemoteConfigPrimitive_worksAndDoesNotGetOverridden() {
        val originalValue = settingsPreferencesManager.parentalConsentExists
        Assert.assertEquals(SettingsPreferencesManagerImpl.PARENTAL_CONSENT_EXISTS_DEFAULT, originalValue)

        settingsPreferencesManager.parentalConsentExists = !originalValue

        val newValue = settingsPreferencesManager.parentalConsentExists

        Assert.assertEquals(originalValue, newValue)

        verifyExactly(2, remoteConfigSpy) { getBoolean(SettingsPreferencesManagerImpl.PARENTAL_CONSENT_EXISTS_KEY) }
    }

    @Test
    fun fetchingOverridableRemoteConfigPrimitive_worksAndBecomesOverridden() {
        val originalValue = settingsPreferencesManager.language
        Assert.assertEquals(SettingsPreferencesManagerImpl.LANGUAGE_DEFAULT, originalValue)

        settingsPreferencesManager.language = SettingsPreferencesManagerImpl.LANGUAGE_DEFAULT + "q"

        val newValue = settingsPreferencesManager.language

        Assert.assertNotEquals(originalValue, newValue)

        verifyOnce(remoteConfigSpy) { getString(SettingsPreferencesManagerImpl.LANGUAGE_KEY) }
    }

    @Test
    fun fetchingRemoteConfigEnum_worksAndDoesNotGetOverridden() {
        val oldMatchGroup = settingsPreferencesManager.matchGroup
        Assert.assertEquals(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT, oldMatchGroup)

        settingsPreferencesManager.matchGroup = Constants.GROUP.MODULE

        val newMatchGroup = settingsPreferencesManager.matchGroup

        Assert.assertEquals(oldMatchGroup, newMatchGroup)

        verifyExactly(2, remoteConfigSpy) { getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) }
    }

    @Test
    fun fetchingRemoteConfigEnum_revertsToDefaultIfSetToUnrecognizedValue() {
        whenever { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) } thenReturn "PROJECT"

        val matchGroup = settingsPreferencesManager.matchGroup

        Assert.assertEquals(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT, matchGroup)
    }

    @Test
    fun fetchingRemoteConfigEnum_revertsToDefaultIfSetToWrongType() {
        whenever { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) } thenReturn "1"

        val matchGroup = settingsPreferencesManager.matchGroup

        Assert.assertEquals(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT, matchGroup)
    }

    @Test
    fun fetchingOverridableRemoteConfigFingerIdMap_worksAndBecomesOverridden() {
        val oldFingerStatus = settingsPreferencesManager.fingerStatus
        Assert.assertEquals(SettingsPreferencesManagerImpl.FINGER_STATUS_DEFAULT, oldFingerStatus)

        settingsPreferencesManager.fingerStatus = NON_DEFAULT_FINGER_STATUS_TARGET

        val newFingerStatus = settingsPreferencesManager.fingerStatus

        Assert.assertEquals(NON_DEFAULT_FINGER_STATUS_TARGET, newFingerStatus)

        verifyOnce(remoteConfigSpy) { getString(SettingsPreferencesManagerImpl.FINGER_STATUS_KEY) }
    }

    @Test
    fun fetchingOverridableRemoteConfigFingerIdMap_worksForNonDefaultValue() {
        whenever { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGER_STATUS_KEY) } thenReturn NON_DEFAULT_FINGER_STATUS_SERIALIZED

        val fingerStatus = settingsPreferencesManager.fingerStatus

        Assert.assertEquals(NON_DEFAULT_FINGER_STATUS_TARGET, fingerStatus)
    }

    @Test
    fun fetchingOverridableRemoteConfigFingerIdMap_throwsIfMalformed() {
        whenever { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGER_STATUS_KEY) } thenReturn MALFORMED_FINGER_STATUS_SERIALIZED

        assertThrows<JsonSyntaxException> {
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
