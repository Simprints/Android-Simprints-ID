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
import com.simprints.clientapi.integration.AppEnrolResponse
import com.simprints.clientapi.integration.appEnrolAction
import com.simprints.clientapi.integration.buildDummySessionEventsManagerMock
import com.simprints.clientapi.integration.commcare.commCareBaseIntentRequest
import com.simprints.clientapi.integration.commcare.commcareEnrolAction
import com.simprints.moduleapi.app.responses.IAppResponse
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare
import java.util.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class CommCareEnrolResponseTest : KoinTest {

    @Before
    fun setUp() {
        Intents.init()

        loadClientApiKoinModules()
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    @Test
    fun anEnrolAppResponse_shouldReturnACommCareEnrolResponse() {
        val appEnrolResponse = AppEnrolResponse(UUID.randomUUID().toString())
        mockAppModuleEnrolResponse(appEnrolResponse)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply { action = commcareEnrolAction })

        assertCommCareEnrolResponse(scenario, appEnrolResponse)
    }

    private fun assertCommCareEnrolResponse(scenario: ActivityScenario<CommCareActivity>, appEnrolResponse: AppEnrolResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.getBundle("odk_intent_bundle")?.let {
            assertThat(it.getString("guid")).isEqualTo(appEnrolResponse.guid)
            assertThat(it.getBoolean("skipCheck")).isEqualTo(false)
        } ?: throw Exception("No bundle found")
    }

    private fun mockAppModuleEnrolResponse(appEnrolResponse: AppEnrolResponse) {

        val intentResultOk = ActivityResult(Activity.RESULT_OK, Intent().apply {
            this.putExtra(IAppResponse.BUNDLE_KEY, appEnrolResponse)
        })
        Intents.intending(hasAction(appEnrolAction)).respondWith(intentResultOk)
    }

    @After
    fun tearDown() {
        Intents.release()
        KoinInjector.unloadClientApiKoinModules()
    }
}
