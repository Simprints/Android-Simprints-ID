package com.simprints.fingerprint.activities.refusal

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.R
import com.simprints.id.Application
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RefusalActivityTest {

    @Before
    fun setUp() {
        Intents.init()
    }

    @Test
    fun launchRefusalActivity_shouldDisplayAllOptionsAndButtons() {
        launchRefusalActivity()

        onView(withId(R.id.rbReligiousConcerns)).check(matches(isDisplayed()))
        onView(withId(R.id.rbDataConcerns)).check(matches(isDisplayed()))
        onView(withId(R.id.rbDoesNotHavePermission)).check(matches(isDisplayed()))
        onView(withId(R.id.rbAppNotWorking)).check(matches(isDisplayed()))
        onView(withId(R.id.rbTooYoung)).check(matches(isDisplayed()))
        onView(withId(R.id.rbSick)).check(matches(isDisplayed()))
        onView(withId(R.id.rbPregnant)).check(matches(isDisplayed()))
        onView(withId(R.id.rbOther)).check(matches(isDisplayed()))

        onView(withId(R.id.btSubmitRefusalForm)).check(matches(isDisplayed()))
        onView(withId(R.id.btScanFingerprints)).check(matches(isDisplayed()))

        onView(withId(R.id.btSubmitRefusalForm)).check(matches(not(isEnabled())))
    }

    @Test
    fun chooseAnOptionInRefusal_shouldEnableSubmitButton() {
        launchRefusalActivity()

        onView(withId(R.id.btSubmitRefusalForm)).check(matches(not(isEnabled())))
        onView(withId(R.id.rbReligiousConcerns)).perform(click())
        onView(withId(R.id.btScanFingerprints)).check(matches(isEnabled()))
    }

    @Test
    fun chooseOtherOption_shouldNotEnableSubmitButton() {
        launchRefusalActivity()

        onView(withId(R.id.btSubmitRefusalForm)).check(matches(not(isEnabled())))
        onView(withId(R.id.rbReligiousConcerns)).perform(click())
        onView(withId(R.id.btSubmitRefusalForm)).check(matches(not(isEnabled())))
    }

    @Test
    fun chooseOtherOptionAndEnterText_shouldEnableSubmitButton() {
        launchRefusalActivity()

        onView(withId(R.id.btSubmitRefusalForm)).check(matches(not(isEnabled())))
        onView(withId(R.id.rbOther)).perform(click())
        onView(withId(R.id.refusalText)).perform(typeText("Reason for other"))
        onView(withId(R.id.btSubmitRefusalForm)).check(matches(isEnabled()))
    }

    private fun launchRefusalActivity(): ActivityScenario<RefusalActivity> =
        ActivityScenario.launch<RefusalActivity>(Intent().apply {
            setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, RefusalActivity::class.qualifiedName!!)
        })
}
