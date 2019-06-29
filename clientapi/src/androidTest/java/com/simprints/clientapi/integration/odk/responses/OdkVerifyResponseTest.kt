package com.simprints.clientapi.integration.odk.responses
import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkVerifyAction
import com.simprints.moduleapi.app.responses.IAppResponseTier
import com.simprints.testtools.common.syntax.key
import com.simprints.testtools.common.syntax.value
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class OdkVerifyResponseTest : BaseClientApiTest() {

    @Test
    fun appModuleSendsAVerifyAppResponse_shouldReturnAOdkVerifyResponse() {
        val appVerifyResponse = AppVerifyResponse(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        )
        mockAppModulResponse(appVerifyResponse, appVerifyAction)

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
}
