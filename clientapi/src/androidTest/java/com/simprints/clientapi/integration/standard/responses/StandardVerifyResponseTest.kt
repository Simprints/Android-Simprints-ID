package com.simprints.clientapi.integration.standard.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.standard.standardBaseIntentRequest
import com.simprints.clientapi.integration.standard.standardVerifyAction
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Verification
import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppResponseTier
import com.simprints.testtools.common.syntax.key
import com.simprints.testtools.common.syntax.value
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class StandardVerifyResponseTest : BaseClientApiTest() {

    @Test
    fun appModuleSendsAVerifyAppResponse_shouldReturnAStandardVerifyResponse() {
        val appVerifyResponse = AppVerifyResponse(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        )
        mockAppModuleResponse(appVerifyResponse, appVerifyAction)

        val scenario =
            ActivityScenario.launch<LibSimprintsActivity>(standardBaseIntentRequest.apply {
                action = standardVerifyAction
                putExtra(verifyGuidField.key(), verifyGuidField.value())
            })

        assertStandardVerifyResponse(scenario, appVerifyResponse)
    }

    private fun assertStandardVerifyResponse(scenario: ActivityScenario<LibSimprintsActivity>,
                                             appVerifyResponse: AppVerifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        scenario.result.resultData.setExtrasClassLoader(Verification::class.java.classLoader)

        result.resultData.extras?.let {
            val verificationResult = it.getParcelable<Verification>(Constants.SIMPRINTS_VERIFICATION)
                ?: throw Throwable("No verification result returned")

            assertEqualIdentification(verificationResult, appVerifyResponse.matchResult)
        } ?: throw Exception("No bundle found")
    }

    private fun assertEqualIdentification(verification: Verification, appMatchResult: IAppMatchResult) {
        assertThat(verification.confidence.toLong()).isEqualTo(appMatchResult.confidence.toLong())
        assertThat(verification.guid).isEqualTo(appMatchResult.guid)
        assertThat(verification.tier.name).isEqualTo(appMatchResult.tier.name)
    }
}
