package com.simprints.id.activities.orchestrator

import android.app.Activity
import android.content.Intent
import androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode
import androidx.test.espresso.contrib.ActivityResultMatchers.hasResultData
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.moduleapi.app.responses.IAppResponse
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrchestratorActivityTest {

    private val activityActivityTestRule = ActivityTestRule<OrchestratorActivityWithMockPresenter>(OrchestratorActivityWithMockPresenter::class.java, false, false)

    @Test
    fun givenOrchestratorActivity_parseIntent_shouldExtractAnAppRequest() {
        val intent = Intent().apply {
            this.putExtra(AppRequest.BUNDLE_KEY, mock<AppEnrolRequest>())
        }
        val activity = activityActivityTestRule.launchActivity(intent)
        assertThat(activity.isFinishing).isFalse()
    }

    @Test
    fun givenOrchestratorActivity_presenterFinishes_activityShouldReturnTheResult() {
        val intent = Intent().apply {
            this.putExtra(AppRequest.BUNDLE_KEY, mock<AppRequest>())
        }
        val activity = activityActivityTestRule.launchActivity(intent)
        whenever(activity.viewPresenter) { fromDomainToAppResponse(any()) } thenReturn mock<IAppResponse>()

        activity.setResultAndFinish(mock())

        assertThat(activity.isFinishing).isTrue()
        with(activityActivityTestRule.activityResult) {
            assertThat(hasResultCode(Activity.RESULT_OK).matches(this)).isTrue()
            assertThat(hasResultData(IntentMatchers.hasExtraWithKey(AppResponse.BUNDLE_KEY)).matches(this)).isTrue()
        }
    }

    class OrchestratorActivityWithMockPresenter : OrchestratorActivity() {
        override fun startPresenter(appRequest: AppRequest) {
            viewPresenter = mock()
        }
    }
}
