package com.simprints.id.activities.settings

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.fingerselection.FingerSelectionActivity
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsPreferenceFragmentTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    lateinit var configManager: ConfigManager

    @Before
    fun setUp() {
        Intents.init()
        AndroidTestConfig().initComponent().testAppComponent.inject(this)
        configManager = app.component.getConfigManager()

        coEvery { configManager.getProjectConfiguration() } returns mockk(relaxed = true) {
            every { general } returns mockk(relaxed = true) {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        coEvery { configManager.getDeviceConfiguration() } returns mockk(relaxed = true)
    }

    @Test
    fun shouldLaunch_fingerSelectionActivity_successfully_whenFingerSelection_isClicked() {
        val scenario = ActivityScenario.launch(SettingsActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withText(R.string.preference_select_fingers_title)).perform(click())
        intended(hasComponent(FingerSelectionActivity::class.java.name))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}
