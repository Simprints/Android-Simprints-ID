package com.simprints.id.activities.settings.fragments.settingsAbout

import android.content.Context
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.R
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.di.TestAppModule
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.IdentificationConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class SettingsAboutFragmentTest {

    companion object {
        private const val VERSION = "VERSION"
        private const val DEVICE_ID = "DEVICE_ID"
        private const val SCANNER_VERSION = "SCANNER_VERSION"
    }

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private val projectConfiguration = mockk<ProjectConfiguration>()
    private val viewModel = mockk<SettingsAboutViewModel>()
    private val settingsAboutViewModelFactory = mockk<SettingsAboutViewModelFactory>()

    private val viewModelModule by lazy {
        TestViewModelModule(
            settingAboutModelFactoryRule = DependencyRule.ReplaceRule {
                settingsAboutViewModelFactory
            }
        )
    }

    private val module by lazy { TestAppModule(app) }

    @Before
    fun setup() {
        UnitTestConfig(module, viewModelModule = viewModelModule).fullSetup()

        mockkStatic("com.simprints.id.tools.extensions.Context_extKt")
        every { any<Context>().packageVersionName } returns VERSION
        every { any<Context>().deviceId } returns DEVICE_ID

        every {
            settingsAboutViewModelFactory.create<SettingsAboutViewModel>(
                any(),
                any()
            )
        } returns viewModel
        every { viewModel.configuration } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<ProjectConfiguration>>().onChanged(projectConfiguration)
            }
        }
        every { viewModel.recentUserActivity } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<RecentUserActivity>>().onChanged(
                    RecentUserActivity(
                        SCANNER_VERSION,
                        "",
                        "",
                        0,
                        0,
                        0,
                        0
                    )
                )
            }
        }
    }

    @Test
    fun `should display the correct preferences`() {
        every { projectConfiguration.identification } returns mockk {
            every { poolType } returns IdentificationConfiguration.PoolType.PROJECT
        }
        every { projectConfiguration.synchronization } returns mockk {
            every { down } returns mockk {
                every { partitionType } returns DownSynchronizationConfiguration.PartitionType.MODULE
            }
        }

        launchFragment<SettingsAboutFragment>().onFragment { fragment ->
            val syncPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_sync_and_search_key))
            assertThat(syncPref?.summary).isEqualTo("Module Sync - Project Search")
            assertThat(syncPref?.title).isEqualTo(fragment.getString(IDR.string.preference_sync_and_search_title))

            val appVersionPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_app_version_key))
            assertThat(appVersionPref?.summary).isEqualTo(VERSION)
            assertThat(appVersionPref?.title).isEqualTo(fragment.getString(IDR.string.preference_app_version_title))

            val deviceIdPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_device_id_key))
            assertThat(deviceIdPref?.summary).isEqualTo(DEVICE_ID)
            assertThat(deviceIdPref?.title).isEqualTo(fragment.getString(IDR.string.preference_device_id_title))

            val logOutPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_logout_key))
            assertThat(logOutPref?.title).isEqualTo(fragment.getString(IDR.string.preference_logout_title))
        }
    }

    @Test
    fun `should display the scanner preference for Fingerprint`() {
        every { projectConfiguration.general } returns mockk {
            every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        }

        launchFragment<SettingsAboutFragment>().onFragment { fragment ->
            val scannerPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_scanner_version_key))
            assertThat(scannerPref?.summary).isEqualTo(SCANNER_VERSION)
            assertThat(scannerPref?.title).isEqualTo(fragment.getString(IDR.string.preference_scanner_version_title))
            assertThat(scannerPref?.isEnabled).isTrue()
        }
    }

    @Test
    fun `should not display the scanner preference for Face`() {
        every { projectConfiguration.general } returns mockk {
            every { modalities } returns listOf(GeneralConfiguration.Modality.FACE)
        }

        launchFragment<SettingsAboutFragment>().onFragment { fragment ->
            val scannerPref =
                fragment.findPreference<Preference>(fragment.getString(R.string.preference_scanner_version_key))
            assertThat(scannerPref?.isEnabled).isFalse()
        }
    }
}
