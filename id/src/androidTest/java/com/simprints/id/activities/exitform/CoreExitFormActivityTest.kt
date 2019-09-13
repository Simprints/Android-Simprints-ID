package com.simprints.id.activities.exitform

import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.exitform.result.CoreExitFormResult
import com.simprints.id.data.exitform.ExitFormReason
import com.simprints.testtools.android.tryOnUiUntilTimeout
import org.hamcrest.CoreMatchers
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

        onView(ViewMatchers.withId(R.id.rbReligiousConcerns)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.rbDataConcerns)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.rbDoesNotHavePermission)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.rbAppNotWorking)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.rbPersonNotPresent)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.rbTooYoung)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.rbOther)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(ViewMatchers.withId(R.id.btSubmitRefusalForm)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.btScanFingerprints)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(ViewMatchers.withId(R.id.btSubmitRefusalForm)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
    }

    @Test
    fun chooseAnOptionInRefusal_shouldEnableSubmitButton() {
        launchCoreExitFormActivity()

        onView(ViewMatchers.withId(R.id.btSubmitRefusalForm)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
        onView(ViewMatchers.withId(R.id.rbReligiousConcerns)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.btScanFingerprints)).check(ViewAssertions.matches(ViewMatchers.isEnabled()))
    }

    @Test
    fun chooseOtherOption_shouldNotEnableSubmitButton() {
        launchCoreExitFormActivity()

        onView(ViewMatchers.withId(R.id.btSubmitRefusalForm)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
        onView(ViewMatchers.withId(R.id.rbOther)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.btSubmitRefusalForm)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
    }

    @Test
    fun chooseOtherOptionAndEnterText_shouldEnableSubmitButton() {
        launchCoreExitFormActivity()

        onView(ViewMatchers.withId(R.id.btSubmitRefusalForm)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
        onView(ViewMatchers.withId(R.id.rbOther)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.exitFormText)).perform(ViewActions.typeText("Reason for other"))
        tryOnUiUntilTimeout(1000, 200) {
            onView(ViewMatchers.withId(R.id.btSubmitRefusalForm)).check(ViewAssertions.matches(ViewMatchers.isEnabled()))
        }
    }

    @Test
    fun chooseOptionEnterTextAndSubmit_shouldFinishWithRightResult() {
        val refusalReasonText = "Reason for refusal"
        val scenario = launchCoreExitFormActivity()

        onView(ViewMatchers.withId(R.id.rbReligiousConcerns)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.exitFormText)).perform(ViewActions.typeText(refusalReasonText))
        onView(ViewMatchers.withId(R.id.btSubmitRefusalForm)).perform(ViewActions.click())

        verifyIntentReturned(scenario.result, CoreExitFormResult.Action.SUBMIT,
            ExitFormReason.REFUSED_RELIGION, refusalReasonText, CoreExitFormResult.RESULT_CODE_SUBMIT)
    }

    @Test
    fun pressScanFingerprint_shouldFinishWithRightResultWithDefaultRefusalFormReason() {
        val scenario = launchCoreExitFormActivity()

        onView(ViewMatchers.withId(R.id.btScanFingerprints)).perform(ViewActions.click())

        verifyIntentReturned(scenario.result, CoreExitFormResult.Action.GO_BACK,
            ExitFormReason.OTHER, "", CoreExitFormResult.RESULT_CODE_GO_BACK)
    }

    private fun verifyIntentReturned(result: Instrumentation.ActivityResult,
                                     action: CoreExitFormResult.Action,
                                     exitReason: ExitFormReason,
                                     exitFormText: String,
                                     expectedResultCode: Int) {
        Truth.assertThat(result.resultCode).isEqualTo(expectedResultCode)

        result.resultData.setExtrasClassLoader(CoreExitFormResult::class.java.classLoader)
        val response = result.resultData.getParcelableExtra<CoreExitFormResult>(CoreExitFormResult.BUNDLE_KEY)

        Truth.assertThat(response).isInstanceOf(CoreExitFormResult::class.java)
        Truth.assertThat(response.action).isEqualTo(action)
        Truth.assertThat(response.answer.reason).isEqualTo(exitReason)
        Truth.assertThat(response.answer.optionalText).isEqualTo(exitFormText)
    }


    private fun launchCoreExitFormActivity(): ActivityScenario<CoreExitFormActivity> =
        ActivityScenario.launch(Intent().apply {
            setClassName(ApplicationProvider.getApplicationContext<Application>().packageName,
                CoreExitFormActivity::class.qualifiedName!!)
        })
}
