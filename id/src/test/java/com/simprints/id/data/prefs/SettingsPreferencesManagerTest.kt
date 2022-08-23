package com.simprints.id.data.prefs

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.GROUP
import com.simprints.core.tools.utils.LanguageHelper.SHARED_PREFS_LANGUAGE_DEFAULT
import com.simprints.core.tools.utils.LanguageHelper.SHARED_PREFS_LANGUAGE_KEY
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.settings.*
import com.simprints.id.data.prefs.settings.fingerprint.models.FingerComparisonStrategy
import com.simprints.id.domain.SimprintsSyncSetting
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.di.TestPreferencesModule
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.every
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SettingsPreferencesManagerTest {

    @Inject
    lateinit var remoteConfigSpy: RemoteConfigWrapper

    @Inject
    lateinit var settingsPreferencesManager: SettingsPreferencesManager

    private val preferencesModule by lazy {
        // Because of this rule, the injections above are Spyk
        TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.SpykRule)
    }

    @Before
    fun setup() {
        UnitTestConfig(preferencesModule = preferencesModule).fullSetup().inject(this)
    }

    @Test
    fun fetchingRemoteConfigPrimitive_worksAndDoesNotGetOverridden() {
        val originalValue = settingsPreferencesManager.logoExists
        assertThat(SettingsPreferencesManagerImpl.LOGO_EXISTS_DEFAULT).isEqualTo(originalValue)

        settingsPreferencesManager.logoExists = !originalValue

        val newValue = settingsPreferencesManager.logoExists

        assertThat(originalValue).isEqualTo(newValue)

        verify(exactly = 2) { remoteConfigSpy.getBoolean(SettingsPreferencesManagerImpl.LOGO_EXISTS_KEY) }
    }

    @Test
    fun fetchingOverridableRemoteConfigPrimitive_worksAndBecomesOverridden() {
        val originalValue = settingsPreferencesManager.language
        assertThat(SHARED_PREFS_LANGUAGE_DEFAULT).isEqualTo(originalValue)

        settingsPreferencesManager.language = SHARED_PREFS_LANGUAGE_DEFAULT + "q"

        val newValue = settingsPreferencesManager.language

        Assert.assertNotEquals(originalValue, newValue)

        verify(exactly = 1) { remoteConfigSpy.getString(SHARED_PREFS_LANGUAGE_KEY) }
    }

    @Test
    fun fetchingRemoteConfigEnum_worksAndDoesNotGetOverridden() {
        val oldMatchGroup = settingsPreferencesManager.matchGroup
        assertThat(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT).isEqualTo(oldMatchGroup)

        settingsPreferencesManager.matchGroup = GROUP.MODULE

        val newMatchGroup = settingsPreferencesManager.matchGroup

        assertThat(oldMatchGroup).isEqualTo(newMatchGroup)

        verify(exactly = 2) { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) }
    }

    @Test
    fun fetchingRemoteConfigEnum_revertsToDefaultIfSetToUnrecognizedValue() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) } returns "PROJECT"

        val matchGroup = settingsPreferencesManager.matchGroup

        assertThat(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT).isEqualTo(matchGroup)
    }

    @Test
    fun fetchingSimprintsSyncReturnsAll() {
        val simprintsSyncSetting = settingsPreferencesManager.simprintsSyncSetting

        assertThat(simprintsSyncSetting).isEqualTo(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_SETTINGS_DEFAULT)

        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_KEY) } returns "ALL"

        val syncSetting = settingsPreferencesManager.simprintsSyncSetting

        assertThat(syncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_ALL)
    }

    @Test
    fun fetchingSimprintsSyncReturnsOnlyBiometrics() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_KEY) } returns "ONLY_BIOMETRICS"

        val syncSetting = settingsPreferencesManager.simprintsSyncSetting

        assertThat(syncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_ONLY_BIOMETRICS)
    }

    @Test
    fun fetchingSimprintsSyncReturnsOnlyAnalytics() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_KEY) } returns "ONLY_ANALYTICS"

        val syncSetting = settingsPreferencesManager.simprintsSyncSetting

        assertThat(syncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_ONLY_ANALYTICS)
    }

    @Test
    fun fetchingSimprintsSyncReturnsNone() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_KEY) } returns "NONE"

        val syncSetting = settingsPreferencesManager.simprintsSyncSetting

        assertThat(syncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_NONE)
    }

    @Test
    fun fetchingSimprintsSyncReturnsEmptyList() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_KEY) } returns null

        val syncSetting = settingsPreferencesManager.simprintsSyncSetting

        assertThat(syncSetting).isEqualTo(SimprintsSyncSetting.SIM_SYNC_NONE)
    }

    @Test
    fun fetchingRemoteConfigEnum_revertsToDefaultIfSetToWrongType() {
        every { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.MATCH_GROUP_KEY) } returns "1"

        val matchGroup = settingsPreferencesManager.matchGroup

        assertThat(SettingsPreferencesManagerImpl.MATCH_GROUP_DEFAULT).isEqualTo(matchGroup)
    }

    @Test
    fun fetchingOverridableRemoteConfigFingersToCollect_worksAndBecomesOverridden() {
        val oldFingersToCollect = settingsPreferencesManager.fingerprintsToCollect
        assertThat(SettingsPreferencesManagerImpl.FINGERPRINTS_TO_COLLECT_DEFAULT).isEqualTo(
            oldFingersToCollect
        )

        settingsPreferencesManager.fingerprintsToCollect = NON_DEFAULT_FINGERS_TO_COLLECT

        val newFingerStatus = settingsPreferencesManager.fingerprintsToCollect

        assertThat(NON_DEFAULT_FINGERS_TO_COLLECT).isEqualTo(newFingerStatus)

        verify(exactly = 1) { remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGERPRINTS_TO_COLLECT_KEY) }
    }


    @Test
    fun fetchingDefaultFingerComparisonStrategy() {
        val oldStrategy = settingsPreferencesManager.fingerComparisonStrategy
        assertThat(SettingsPreferencesManagerImpl.FINGERPRINT_COMPARISON_STRATEGY_FOR_VERIFICATION_DEFAULT).isEqualTo(
            oldStrategy
        )
        settingsPreferencesManager.fingerComparisonStrategy =
            NON_DEFAULT_FINGERS_COMPARISON_STRATEGY
    }

    @Test
    fun fetchingOverridableFingerComparisonStrategy() {
        every {
            remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGERPRINT_MATCHING_STRATEGY_FOR_VERIFICATION_KEY)
        } returns NON_DEFAULT_FINGERS_COMPARISON_STRATEGY_SERIALIZED

        val newStrategy = settingsPreferencesManager.fingerComparisonStrategy
        assertThat(NON_DEFAULT_FINGERS_COMPARISON_STRATEGY).isEqualTo(newStrategy)
    }

    @Test
    fun fetchingOverridableRemoteConfigFingerIdMap_worksForNonDefaultValue() {
        every {
            remoteConfigSpy.getString(SettingsPreferencesManagerImpl.FINGERPRINTS_TO_COLLECT_KEY)
        } returns NON_DEFAULT_FINGERS_TO_COLLECT_SERIALIZED

        val fingerStatus = settingsPreferencesManager.fingerprintsToCollect

        assertThat(NON_DEFAULT_FINGERS_TO_COLLECT).isEqualTo(fingerStatus)
    }

    @Test
    fun simprintsSyncSettingNone_returnsCorrectValuesForConvenienceMethods() {
        every {
            remoteConfigSpy.getString(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_KEY)
        } returns "NONE"

        assertFalse { settingsPreferencesManager.canSyncDataToSimprints() }
        assertFalse { settingsPreferencesManager.canSyncAllDataToSimprints() }
        assertFalse { settingsPreferencesManager.canSyncBiometricDataToSimprints() }
        assertFalse { settingsPreferencesManager.canSyncAnalyticsDataToSimprints() }
    }

    @Test
    fun simprintsSyncSettingAll_returnsCorrectValuesForConvenienceMethods() {
        every {
            remoteConfigSpy.getString(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_KEY)
        } returns "ALL"

        assertTrue { settingsPreferencesManager.canSyncDataToSimprints() }
        assertTrue { settingsPreferencesManager.canSyncAllDataToSimprints() }
        assertFalse { settingsPreferencesManager.canSyncBiometricDataToSimprints() }
        assertFalse { settingsPreferencesManager.canSyncAnalyticsDataToSimprints() }
    }

    @Test
    fun simprintsSyncSettingBiometrics_returnsCorrectValuesForConvenienceMethods() {
        every {
            remoteConfigSpy.getString(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_KEY)
        } returns "ONLY_BIOMETRICS"

        assertTrue { settingsPreferencesManager.canSyncDataToSimprints() }
        assertFalse { settingsPreferencesManager.canSyncAllDataToSimprints() }
        assertTrue { settingsPreferencesManager.canSyncBiometricDataToSimprints() }
        assertFalse { settingsPreferencesManager.canSyncAnalyticsDataToSimprints() }
    }

    @Test
    fun simprintsSyncSettingAnalytics_returnsCorrectValuesForConvenienceMethods() {
        every {
            remoteConfigSpy.getString(SettingsPreferencesManagerImpl.SIMPRINTS_SYNC_KEY)
        } returns "ONLY_ANALYTICS"

        assertTrue { settingsPreferencesManager.canSyncDataToSimprints() }
        assertFalse { settingsPreferencesManager.canSyncAllDataToSimprints() }
        assertFalse { settingsPreferencesManager.canSyncBiometricDataToSimprints() }
        assertTrue { settingsPreferencesManager.canSyncAnalyticsDataToSimprints() }
    }

    @Test
    fun simprintsCanDownSyncEvents_returnsFalseWhenOFF() {
        every {
            remoteConfigSpy.getString(SettingsPreferencesManagerImpl.PEOPLE_DOWN_SYNC_SETTING_KEY)
        } returns "OFF"

        assertFalse { settingsPreferencesManager.canDownSyncEvents() }
    }

    @Test
    fun simprintsCanDownSyncEvents_returnsTrueWhenON() {
        every {
            remoteConfigSpy.getString(SettingsPreferencesManagerImpl.PEOPLE_DOWN_SYNC_SETTING_KEY)
        } returns "ON"

        assertTrue { settingsPreferencesManager.canDownSyncEvents() }
    }

    @Test
    fun simprintsCanDownSyncEvents_returnsTrueWhenEXTRA() {
        every {
            remoteConfigSpy.getString(SettingsPreferencesManagerImpl.PEOPLE_DOWN_SYNC_SETTING_KEY)
        } returns "EXTRA"

        assertTrue { settingsPreferencesManager.canDownSyncEvents() }
    }

    companion object {

        private const val NON_DEFAULT_FINGERS_TO_COLLECT_SERIALIZED =
            "RIGHT_4TH_FINGER,RIGHT_5TH_FINGER,LEFT_THUMB,LEFT_5TH_FINGER"
        private val NON_DEFAULT_FINGERS_TO_COLLECT = listOf(
            FingerIdentifier.RIGHT_4TH_FINGER,
            FingerIdentifier.RIGHT_5TH_FINGER,
            FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.LEFT_5TH_FINGER
        )
        private const val NON_DEFAULT_FINGERS_COMPARISON_STRATEGY_SERIALIZED =
            "CROSS_FINGER_USING_MEAN_OF_MAX"
        private val NON_DEFAULT_FINGERS_COMPARISON_STRATEGY =
            FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
    }
}
