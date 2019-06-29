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
import com.simprints.clientapi.integration.AppIdentifyResponse
import com.simprints.clientapi.integration.AppMatchResult
import com.simprints.clientapi.integration.appIdentifyAction
import com.simprints.clientapi.integration.buildDummySessionEventsManagerMock
import com.simprints.clientapi.integration.commcare.commCareBaseIntentRequest
import com.simprints.clientapi.integration.commcare.commcareIdentifyAction
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
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
class CommCareIdentifyResponseTest : KoinTest {

    @Before
    fun setUp() {
        Intents.init()

        loadClientApiKoinModules()
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    @Test
    fun anIdentifyAppResponse_shouldReturnACommCareIdentifyResponse() {
        val appIdentifyResponse = AppIdentifyResponse(listOf(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        ), "session_id")
        mockAppModuleIdentifyResponse(appIdentifyResponse)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply { action = commcareIdentifyAction })

        assertCommCareIdentifyResponse(scenario, appIdentifyResponse)
    }

    private fun assertCommCareIdentifyResponse(scenario: ActivityScenario<CommCareActivity>,
                                               appIdentifyResponse: AppIdentifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            scenario.result.resultData.setExtrasClassLoader(Identification::class.java.classLoader)

            assertThat(it.getParcelableArray(Constants.SIMPRINTS_IDENTIFICATIONS)).isSameAs(appIdentifyResponse.identifications)
            assertThat(it.getBoolean("skipCheck")).isEqualTo(false)
        } ?: throw Exception("No bundle found")
    }

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
