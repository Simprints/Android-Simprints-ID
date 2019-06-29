package com.simprints.clientapi.integration.commcare.responses
import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.di.KoinInjector
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.commcare.commCareBaseIntentRequest
import com.simprints.clientapi.integration.commcare.commcareVerifyAction
import com.simprints.moduleapi.app.responses.IAppResponse
import com.simprints.moduleapi.app.responses.IAppResponseTier
import com.simprints.testtools.common.syntax.key
import com.simprints.testtools.common.syntax.value
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare
import java.util.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class CommCareVerifyResponseTest : KoinTest {

    @Before
    fun setUp() {
        Intents.init()

        loadClientApiKoinModules()
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    @Test
    fun anVerifyAppResponse_shouldReturnACommCareVerifyResponse() {
        val appVerifyResponse = AppVerifyResponse(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        )
        mockAppModuleVerifyResponse(appVerifyResponse)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply {
                action = commcareVerifyAction
                putExtra(verifyGuidField.key(), verifyGuidField.value())
            })

        assertCommCareVerifyResponse(scenario, appVerifyResponse)
    }

    private fun assertCommCareVerifyResponse(scenario: ActivityScenario<CommCareActivity>, appVerifyResponse: AppVerifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.getBundle("odk_intent_bundle")?.let {

            assertThat(it.getString("guid")).isEqualTo(appVerifyResponse.matchResult.guid)
            assertThat(it.getString("tier")).isEqualTo(appVerifyResponse.matchResult.tier.name)
            assertThat(it.getInt("confidence")).isEqualTo(appVerifyResponse.matchResult.confidence)
            assertThat(it.getBoolean("skipCheck")).isEqualTo(false)
        } ?: throw Exception("No bundle found")
    }

    private fun mockAppModuleVerifyResponse(appVerifyResponse: AppVerifyResponse) {

        val intentResultOk = ActivityResult(Activity.RESULT_OK, Intent().apply {
            this.putExtra(IAppResponse.BUNDLE_KEY, appVerifyResponse)
        })
        Intents.intending(hasAction(appVerifyAction)).respondWith(intentResultOk)
    }

    @After
    fun tearDown() {
        Intents.release()
        KoinInjector.unloadClientApiKoinModules()
    }
}
