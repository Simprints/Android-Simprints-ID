package com.simprints.clientapi.integration.odk.responses
import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.di.KoinInjector
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkVerifyAction
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
class OdkVerifyResponseTest : KoinTest {

    @Before
    fun setUp() {
        Intents.init()

        loadClientApiKoinModules()
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    @Test
    fun anVerifyAppResponse_shouldReturnAOdkVerifyResponse() {
        val appVerifyResponse = AppVerifyResponse(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        )
        mockAppModuleVerifyResponse(appVerifyResponse)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply {
                action = odkVerifyAction
                putExtra(verifyGuidField.key(), verifyGuidField.value())
            })

        assertOdkVerifyResponse(scenario, appVerifyResponse)
    }

    private fun assertOdkVerifyResponse(scenario: ActivityScenario<OdkActivity>, appVerifyResponse: AppVerifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getString("odk-guids")).isEqualTo(appVerifyResponse.matchResult.guid)
            assertThat(it.getString("odk-confidences")).isEqualTo(appVerifyResponse.matchResult.confidence)
            assertThat(it.getString("odk-tiers")).isEqualTo(appVerifyResponse.matchResult.tier.name)
            assertThat(it.getBoolean("odk-skip-check")).isFalse()
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
