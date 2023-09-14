package com.simprints.clientapi.integration.odk.requests

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppEnrolRequest
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import com.simprints.clientapi.integration.value
import com.simprints.moduleapi.app.requests.IAppRequest
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OdkEnrolRequestTest : BaseOdkClientApiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        val intentResultOk = ActivityResult(Activity.RESULT_OK, null)
        intending(hasAction(APP_ENROL_ACTION)).respondWith(intentResultOk)
    }

    @Test
    fun callingAppSendsAnEnrolRequest_shouldLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(odkBaseFlowIntentRequest.apply { action = ODK_ENROL_ACTION })

        val expectedAppRequest = AppEnrolRequest(
            projectId = projectIdField.value(),
            userId = userIdField.value(),
            isUserIdTokenized = false,
            moduleId = moduleIdField.value(),
            isModuleIdTokenized = false,
            metadata = metadataField.value()
        )

        intended(hasAction(APP_ENROL_ACTION))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun callingAppSendsASuspiciousEnrolRequest_shouldLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(makeIntentRequestSuspicious(odkBaseFlowIntentRequest).apply { action = ODK_ENROL_ACTION })
        intended(hasAction(APP_ENROL_ACTION))
    }

    @Test
    fun callingAppSendsAnInvalidEnrolRequest_shouldNotLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(makeIntentRequestInvalid(odkBaseFlowIntentRequest).apply { action = ODK_ENROL_ACTION })
        intended(not(hasAction(APP_ENROL_ACTION)), times(2))
    }
}
