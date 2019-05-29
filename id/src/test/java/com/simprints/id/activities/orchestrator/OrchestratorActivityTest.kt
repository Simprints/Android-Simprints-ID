package com.simprints.id.activities.orchestrator

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import com.simprints.id.Application
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.moduleapi.app.responses.IAppEnrolResponse
import com.simprints.moduleapi.app.responses.IAppResponse
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import org.jetbrains.anko.doAsync
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class OrchestratorActivityTest {

    companion object {
        const val NEXT_ACTIVITY_ACTION = "com.next.activity"
        const val REQUEST_CODE_NEXT_ACTIVITY = 1
    }

    private val nextActivity = Intent().apply { action = NEXT_ACTIVITY_ACTION }
    private val returnNextActivity = Intent()

    private lateinit var activityScenario: ActivityScenario<OrchestratorActivity>

    @Before
    fun setup() {
        mockOrchestratorDI()
        activityScenario = createScenarioForOrchestratorActivity()
        mockResultForNextActivity()
    }

    @Test
    fun givenOrchestratorActivity_onCreate_aValidAppRequestIsSetOnThePresenter() {
        activityScenario.onActivity {
            verify(it.viewPresenter).appRequest = anyNotNull()
        }
    }

    @Test
    fun givenOrchestratorActivity_aResultIsReceivedFromNextActivity_shouldBeForwardedToPresenter() {
        activityScenario.onActivity {

            startNextActivity(it)

            verifyPresenterReceivesTheResultFromNextActivity(it.viewPresenter)
        }
    }

    @Test
    fun givenOrchestratorActivity_presenterReturnsAnError_shouldFinishWithAnError() {
        activityScenario.onActivity {

            it.setCancelResultAndFinish()

            assertThat(it.isFinishing).isTrue()
            assertThat(activityScenario.result.resultCode).isEqualTo(RESULT_CANCELED)
        }
    }

    @Test
    fun givenOrchestratorActivity_presenterReturnsAValidAppResponse_shouldFinishWithAnResponse() {
        activityScenario.onActivity {

            it.setResultAndFinish(mock<AppEnrolResponse>())

            assertThat(it.isFinishing).isTrue()
            val resultIntent = activityScenario.result
            val resultData = resultIntent.resultData.getParcelableExtra<IAppResponse>(IAppResponse.BUNDLE_KEY)
            assertThat(resultData).isNotInstanceOf(IAppEnrolResponse::class.java)
            assertThat(resultIntent.resultCode).isNotInstanceOf(IAppEnrolResponse::class.java)
        }
    }


    private fun mockResultForNextActivity() {
        Intents.init()
        intending(hasAction(nextActivity.action)).respondWith(ActivityResult(RESULT_OK, returnNextActivity))
    }

    private fun createScenarioForOrchestratorActivity(): ActivityScenario<OrchestratorActivity> =
        ActivityScenario.launch<OrchestratorActivity>(Intent().apply {
            setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, OrchestratorActivity::class.qualifiedName!!)
            val appRequest = AppEnrolRequest("project_id", "user_id", "module_id", "")
            putExtra(AppRequest.BUNDLE_KEY, appRequest)
        })

    private fun startNextActivity(activity: OrchestratorActivity) {
        activity.startNextActivity(REQUEST_CODE_NEXT_ACTIVITY, nextActivity)
    }

    private fun verifyPresenterReceivesTheResultFromNextActivity(viewPresenter: OrchestratorContract.Presenter) {
        doAsync {
            verifyOnce(viewPresenter) {
                handleResult(REQUEST_CODE_NEXT_ACTIVITY, android.app.Activity.RESULT_OK, returnNextActivity)
            }
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        Intents.release()
    }
}
