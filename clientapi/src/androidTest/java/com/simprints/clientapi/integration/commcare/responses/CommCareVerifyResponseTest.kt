package com.simprints.clientapi.integration.commcare.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.AppMatchResult
import com.simprints.clientapi.integration.AppVerifyResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.commcare.BaseCommCareClientApiTest
import com.simprints.moduleapi.app.responses.IAppResponseTier
import com.simprints.testtools.common.syntax.key
import com.simprints.testtools.common.syntax.value
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class CommCareVerifyResponseTest : BaseCommCareClientApiTest() {

    @Test
    fun appModuleSendsAVerifyAppResponse_shouldReturnACommCareVerifyResponse() {
        val appVerifyResponse = AppVerifyResponse(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        )
        mockAppModuleResponse(appVerifyResponse, APP_VERIFICATION_ACTION)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply {
                action = COMMCARE_VERIFY_ACTION
                putExtra(verifyGuidField.key(), verifyGuidField.value())
            })

        assertCommCareVerifyResponse(scenario, appVerifyResponse)
    }

    private fun assertCommCareVerifyResponse(scenario: ActivityScenario<CommCareActivity>, appVerifyResponse: AppVerifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.getBundle(COMMCARE_BUNDLE_KEY)?.let {

            assertThat(it.getString(VERIFICATION_GUID_KEY)).isEqualTo(appVerifyResponse.matchResult.guid)
            assertThat(it.getString(VERIFICATION_TIER_KEY)).isEqualTo(appVerifyResponse.matchResult.tier.name)
            assertThat(it.getInt(VERIFICATION_CONFIDENCE_KEY)).isEqualTo(appVerifyResponse.matchResult.confidence)
            assertThat(it.getString(SKIP_CHECK_KEY)).isEqualTo(BaseClientApiTest.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW.toString())
        } ?: throw Exception("No bundle found")
    }
}
