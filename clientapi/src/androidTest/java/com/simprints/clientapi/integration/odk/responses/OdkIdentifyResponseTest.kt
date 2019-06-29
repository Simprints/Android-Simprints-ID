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
import com.simprints.clientapi.integration.AppIdentifyResponse
import com.simprints.clientapi.integration.AppMatchResult
import com.simprints.clientapi.integration.appIdentifyAction
import com.simprints.clientapi.integration.buildDummySessionEventsManagerMock
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkIdentifyAction
import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppResponse
import com.simprints.moduleapi.app.responses.IAppResponseTier
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare
import java.util.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class OdkIdentifyResponseTest : KoinTest {

    @Before
    fun setUp() {
        Intents.init()

        loadClientApiKoinModules()
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    @Test
    fun anIdentifyAppResponse_shouldReturnAOdkIdentifyResponse() {
        val appIdentifyResponse = AppIdentifyResponse(listOf(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        ), "session_id")
        mockAppModuleIdentifyResponse(appIdentifyResponse)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = odkIdentifyAction })

        assertOdkIdentifyResponse(scenario, appIdentifyResponse)
    }

    private fun assertOdkIdentifyResponse(scenario: ActivityScenario<OdkActivity>,
                                               appIdentifyResponse: AppIdentifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getString("odk-guids")).isEqualTo(guidsInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString("odk-confidences")).isEqualTo(confidenceScoresInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString("odk-tiers")).isEqualTo(tiersInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString("odk-session-id")).isEqualTo(appIdentifyResponse.sessionId)
            assertThat(it.getBoolean("odk-skip-check")).isFalse()
        } ?: throw Exception("No bundle found")
    }

    private fun confidenceScoresInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.joinToString(separator = "", transform = { "${it.confidence} " }).trimEnd()

    private fun guidsInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.joinToString(separator = "", transform = { "${it.guid} " }).trimEnd()

    private fun tiersInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.joinToString(separator = "", transform = { "${it.tier} " }).trimEnd()


    private fun mockAppModuleIdentifyResponse(appIdentifyResponse: AppIdentifyResponse) {

        val intentResultOk = ActivityResult(Activity.RESULT_OK, Intent().apply {
            this.putExtra(IAppResponse.BUNDLE_KEY, appIdentifyResponse)
        })
        Intents.intending(hasAction(appIdentifyAction)).respondWith(intentResultOk)
    }

    @After
    fun tearDown() {
        Intents.release()
        KoinInjector.unloadClientApiKoinModules()
    }
}
