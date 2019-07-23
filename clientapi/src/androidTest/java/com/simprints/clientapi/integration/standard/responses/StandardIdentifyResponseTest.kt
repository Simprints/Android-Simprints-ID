package com.simprints.clientapi.integration.standard.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.AppIdentifyResponse
import com.simprints.clientapi.integration.AppMatchResult
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.standard.BaseStandardClientApiTest
import com.simprints.libsimprints.Constants.SIMPRINTS_IDENTIFICATIONS
import com.simprints.libsimprints.Identification
import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppResponseTier
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class StandardIdentifyResponseTest: BaseStandardClientApiTest() {

    @Test
    fun appModuleSendsAnIdentifyAppResponse_shouldReturnAStandardIdentifyResponse() {
        val appIdentifyResponse = AppIdentifyResponse(listOf(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        ), "session_id")
        mockAppModuleResponse(appIdentifyResponse, APP_IDENTIFY_ACTION)

        val scenario =
            ActivityScenario.launch<LibSimprintsActivity>(standardBaseIntentRequest.apply { action = STANDARD_IDENTIFY_ACTION })

        assertStandardIdentifyResponse(scenario, appIdentifyResponse)
    }

    private fun assertStandardIdentifyResponse(scenario: ActivityScenario<LibSimprintsActivity>,
                                               appIdentifyResponse: AppIdentifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        scenario.result.resultData.setExtrasClassLoader(Identification::class.java.classLoader)

        result.resultData.extras?.let {
            val identificationsReturned = it.getParcelableArrayList<Identification>(SIMPRINTS_IDENTIFICATIONS)
                ?: throw Throwable("No identifications returned")

            assertEqualIdentification(identificationsReturned[0], appIdentifyResponse.identifications[0])
        } ?: throw Exception("No bundle found")
    }

    private fun assertEqualIdentification(identification: Identification, appMatchResult: IAppMatchResult) {
        assertThat(identification.confidence.toLong()).isEqualTo(appMatchResult.confidence.toLong())
        assertThat(identification.guid).isEqualTo(appMatchResult.guid)
        assertThat(identification.tier.name).isEqualTo(appMatchResult.tier.name)
    }
}
