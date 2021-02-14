package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.secure.SignerManager
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
@ExperimentalCoroutinesApi
class SettingsAboutViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    lateinit var settingsAboutViewModel: SettingsAboutViewModel

    @MockK
    lateinit var preferencesManager: PreferencesManager

    @MockK
    lateinit var signerManager: SignerManager

    @MockK
    lateinit var recentEventsManager: RecentEventsPreferencesManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun vmLogout_dbManagerShouldSignOut() = runBlockingTest {
        buildSettingsViewModel()
        settingsAboutViewModel.logout()

        coVerify { settingsAboutViewModel.signerManager.signOut() }
    }

    private fun buildSettingsViewModel() {
        settingsAboutViewModel = SettingsAboutViewModel(preferencesManager, signerManager, recentEventsManager)
    }
}
