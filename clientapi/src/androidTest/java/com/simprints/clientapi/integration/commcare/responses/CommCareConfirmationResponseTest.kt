package com.simprints.clientapi.integration.commcare.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.integration.AppConfirmationResponse
import com.simprints.clientapi.integration.commcare.BaseCommCareClientApiTest
import com.simprints.clientapi.integration.value
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class CommCareConfirmationResponseTest : BaseCommCareClientApiTest() {
    private  val clientApiSessionEventsManager: ClientApiSessionEventsManager = mockk(relaxed = true){
        coEvery { isSessionHasIdentificationCallback(sessionIdField.value()) } returns true
    }
    @Before
    override fun setUp() {
        super.setUp()
        loadKoinModules(module {
            factory { clientApiSessionEventsManager }
        })
    }
    @Test
    fun appModuleSendsAConfirmationAppResponse_shouldReturnACommCareConfirmationResponse() {
        val appIdentificationOutcomeResponse = AppConfirmationResponse(true)
        mockAppModuleResponse(appIdentificationOutcomeResponse, APP_CONFIRM_ACTION)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareConfirmIntentRequest.apply { action = COMMCARE_CONFIRM_IDENTITY_ACTION })

        assertCommCareConfirmationResponse(scenario)
    }

    private fun assertCommCareConfirmationResponse(scenario: ActivityScenario<CommCareActivity>) {
        with(scenario.result) {
            Truth.assertThat(resultCode).isEqualTo(Activity.RESULT_OK)
            resultData.extras?.getBundle(COMMCARE_BUNDLE_KEY)?.let {
                Truth.assertThat(it.getString(BIOMETRICS_COMPLETE_KEY)).isEqualTo(RETURN_FOR_FLOW_COMPLETED.toString())
            } ?: throw Exception("No bundle found")
        }
    }
}
