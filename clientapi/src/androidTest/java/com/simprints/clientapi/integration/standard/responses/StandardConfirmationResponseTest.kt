package com.simprints.clientapi.integration.standard.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.integration.AppConfirmationResponse
import com.simprints.clientapi.integration.standard.BaseStandardClientApiTest
import com.simprints.clientapi.integration.value
import com.simprints.libsimprints.Constants
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class StandardConfirmationResponseTest : BaseStandardClientApiTest() {
    private val clientApiSessionEventsManager: ClientApiSessionEventsManager =
        mockk(relaxed = true) {
            coEvery { isSessionHasIdentificationCallback(sessionIdField.value()) } returns true
            coEvery { getCurrentSessionId() } returns sessionIdField.value()
        }

    @Before
    override fun setUp() {
        super.setUp()
        loadKoinModules(module {
            factory { clientApiSessionEventsManager }
        })
    }
    @Test
    fun appModuleSendsAStandardConfirmationAppResponse_shouldReturnAStandardConfirmationResponse() {
        val appIdentificationOutcomeResponse = AppConfirmationResponse(true)
        mockAppModuleResponse(appIdentificationOutcomeResponse, APP_CONFIRM_ACTION)

        val scenario =
            ActivityScenario.launch<LibSimprintsActivity>(standardConfirmIntentRequest.apply { action = STANDARD_CONFIRM_IDENTITY_ACTION })

        assertStandardConfirmationResponse(scenario)
    }

    private fun assertStandardConfirmationResponse(scenario: ActivityScenario<LibSimprintsActivity>) {
        with(scenario.result) {
            Truth.assertThat(resultCode).isEqualTo(Activity.RESULT_OK)
            resultData.extras?.let {
                Truth.assertThat(it.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isEqualTo(RETURN_FOR_FLOW_COMPLETED)
            } ?: throw Exception("No bundle found")
        }
    }
}
