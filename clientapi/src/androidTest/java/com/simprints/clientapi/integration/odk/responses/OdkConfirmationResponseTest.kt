package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.integration.AppConfirmationResponse
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import com.simprints.clientapi.integration.value
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class OdkConfirmationResponseTest : BaseOdkClientApiTest() {
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
    fun appModuleSendsAConfirmationAppResponse_shouldReturnAOdkConfirmationResponse() {
        val appIdentificationOutcomeResponse = AppConfirmationResponse(true)
        mockAppModuleResponse(appIdentificationOutcomeResponse, APP_CONFIRM_ACTION)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkConfirmIntentRequest.apply { action = ODK_CONFIRM_IDENTITY_ACTION })

        assertOdkConfirmationResponse(scenario)
    }

    private fun assertOdkConfirmationResponse(scenario: ActivityScenario<OdkActivity>) {
        with(scenario.result) {
            Truth.assertThat(resultCode).isEqualTo(Activity.RESULT_OK)
            resultData.extras?.let {
                Truth.assertThat(it.getBoolean(ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE)).isEqualTo(RETURN_FOR_FLOW_COMPLETED)
            } ?: throw Exception("No bundle found")
        }
    }
}
