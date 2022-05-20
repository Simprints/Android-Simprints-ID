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
import com.simprints.id.commontesttools.di.TestAppModule
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SettingsPreferenceFragmentTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val appModule by lazy { TestAppModule(app) }

    @Before
    fun setUp() {
        Intents.init()
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
