package com.simprints.fingerprint.activities.refusal

import android.app.Activity
import android.app.Instrumentation
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
import com.google.common.truth.Truth
import com.simprints.fingerprint.R
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.id.Application
import org.hamcrest.CoreMatchers.not
import org.junit.After
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
        onView(withId(R.id.rbPersonNotPresent)).check(matches(isDisplayed()))
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
        onView(withId(R.id.rbOther)).perform(click())
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

    @Test
    fun chooseOptionEnterTextAndSubmit_shouldFinishWithRightResult() {
        val refusalReasonText = "Reason for refusal"
        val scenario = launchRefusalActivity()

        onView(withId(R.id.rbReligiousConcerns)).perform(click())
        onView(withId(R.id.refusalText)).perform(typeText(refusalReasonText))
        onView(withId(R.id.btSubmitRefusalForm)).perform(click())

        verifyIntentReturned(scenario.result, RefusalActResult.Action.SUBMIT,
            RefusalFormReason.REFUSED_RELIGION, refusalReasonText)
    }

    private fun launchRefusalActivity(): ActivityScenario<RefusalActivity> =
        ActivityScenario.launch<RefusalActivity>(Intent().apply {
            setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, RefusalActivity::class.qualifiedName!!)
        })

    private fun verifyIntentReturned(result: Instrumentation.ActivityResult,
                                     action: RefusalActResult.Action,
                                     refusalReason: RefusalFormReason,
                                     refusalReasonText: String) {
        Truth.assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)

        result.resultData.setExtrasClassLoader(RefusalActResult::class.java.classLoader)
        val response = result.resultData.getParcelableExtra<RefusalActResult>(RefusalActResult.BUNDLE_KEY)

        Truth.assertThat(response).isInstanceOf(RefusalActResult::class.java)
        Truth.assertThat(response.action).isEqualTo(action)
        Truth.assertThat(response.answer.reason).isEqualTo(refusalReason)
        Truth.assertThat(response.answer.optionalText).isEqualTo(refusalReasonText)
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}
