package com.simprints.id.activities.coreexitform

import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.R
import com.simprints.id.activities.coreexitform.result.CoreExitFormActivityResult
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.exitformhandler.ExitFormResult.Companion.EXIT_FORM_BUNDLE_KEY
import com.simprints.testtools.android.tryOnUiUntilTimeout
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CoreExitFormActivityTest {

    @Before
    fun setUp() {
        Intents.init()
    }

    @Test
    fun launchRefusalActivity_shouldDisplayAllOptionsAndButtons() {
        launchCoreExitFormActivity()

        onView(withId(R.id.rbReligiousConcerns)).check(matches(isDisplayed()))
        onView(withId(R.id.rbDataConcerns)).check(matches(isDisplayed()))
        onView(withId(R.id.rbDoesNotHavePermission)).check(matches(isDisplayed()))
        onView(withId(R.id.rbAppNotWorking)).check(matches(isDisplayed()))
        onView(withId(R.id.rbPersonNotPresent)).check(matches(isDisplayed()))
        onView(withId(R.id.rbTooYoung)).check(matches(isDisplayed()))
        onView(withId(R.id.rbOther)).check(matches(isDisplayed()))

        onView(withId(R.id.btSubmitExitForm)).check(matches(isDisplayed()))
        onView(withId(R.id.btGoBack)).check(matches(isDisplayed()))

        onView(withId(R.id.btSubmitExitForm)).check(matches(not(isEnabled())))
    }

    @Test
    fun chooseAnOptionInRefusal_shouldEnableSubmitButton() {
        launchCoreExitFormActivity()

        onView(withId(R.id.btSubmitExitForm)).check(matches(not(isEnabled())))
        onView(withId(R.id.rbReligiousConcerns)).perform(click())
        onView(withId(R.id.btGoBack)).check(matches(isEnabled()))
    }

    @Test
    fun chooseOtherOption_shouldNotEnableSubmitButton() {
        launchCoreExitFormActivity()

        onView(withId(R.id.btSubmitExitForm)).check(matches(not(isEnabled())))
        onView(withId(R.id.rbOther)).perform(click())
        onView(withId(R.id.btSubmitExitForm)).check(matches(not(isEnabled())))
    }

    @Test
    fun chooseOtherOptionAndEnterText_shouldEnableSubmitButton() {
        launchCoreExitFormActivity()

        onView(withId(R.id.btSubmitExitForm)).check(matches(not(isEnabled())))
        onView(withId(R.id.rbOther)).perform(click())
        onView(withId(R.id.exitFormText)).perform(typeText("Reason for other"))
        tryOnUiUntilTimeout(1000, 200) {
            onView(withId(R.id.btSubmitExitForm)).check(matches(isEnabled()))
        }
    }

    @Test
    fun chooseOptionEnterTextAndSubmit_shouldFinishWithRightResult() {
        val refusalReasonText = "Reason for refusal"
        val scenario = launchCoreExitFormActivity()

        onView(withId(R.id.rbReligiousConcerns)).perform(click())
        onView(withId(R.id.exitFormText)).perform(typeText(refusalReasonText), closeSoftKeyboard())
        onView(withId(R.id.btSubmitExitForm)).perform(click())

        verifyIntentReturned(scenario.result, CoreExitFormActivityResult.Action.SUBMIT,
            CoreExitFormReason.REFUSED_RELIGION, refusalReasonText)
    }

    @Test
    fun pressScanFingerprint_shouldFinishWithRightResultWithDefaultRefusalFormReason() {
        val scenario = launchCoreExitFormActivity()

        onView(withId(R.id.btGoBack)).perform(click())

        verifyIntentReturned(scenario.result, CoreExitFormActivityResult.Action.GO_BACK,
            CoreExitFormReason.OTHER, "")
    }

    private fun verifyIntentReturned(result: Instrumentation.ActivityResult,
                                     action: CoreExitFormActivityResult.Action,
                                     coreExitReason: CoreExitFormReason,
                                     exitFormText: String) {

        result.resultData.setExtrasClassLoader(CoreExitFormActivityResult::class.java.classLoader)
        val response = result.resultData.getParcelableExtra<CoreExitFormActivityResult>(EXIT_FORM_BUNDLE_KEY)

        assertThat(response).isInstanceOf(CoreExitFormActivityResult::class.java)
        assertThat(response.action).isEqualTo(action)
        assertThat(response.answer.reason).isEqualTo(coreExitReason)
        assertThat(response.answer.optionalText).isEqualTo(exitFormText)
    }


    private fun launchCoreExitFormActivity(): ActivityScenario<CoreExitFormActivity> =
        ActivityScenario.launch(CoreExitFormActivity::class.java)
}
