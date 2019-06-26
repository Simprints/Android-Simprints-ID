package com.simprints.clientapi.integration.commcare.requests

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.di.KoinInjector.Companion.loadClientApiKoinModules
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.android.bundleDataMatcherForParcelable
import com.simprints.testtools.common.syntax.key
import com.simprints.testtools.common.syntax.value
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare

@RunWith(AndroidJUnit4::class)
class VerifyRequestTest : KoinTest {

    @Before
    fun setUp() {
        Intents.init()

        loadClientApiKoinModules()
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    @Test
    fun aVerifyRequest_shouldLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<CommCareActivity>(baseIntentRequest.apply {
            action = commcareVerifyAction
            putExtra(verifyGuidField.key(), verifyGuidField.value())
        })

        val expectedAppRequest = AppVerifyRequest(
            projectIdField.value(),
            userIdField.value(),
            moduleIdField.value(),
            metadataField.value(),
            verifyGuidField.value())

        intended(hasAction(appVerifyAction))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun aSuspiciousVerifyRequest_shouldLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<CommCareActivity>(suspiciousIntentRequest.apply {
            action = commcareVerifyAction
            putExtra(verifyGuidField.key(), verifyGuidField.value()) 
        })
        
        intended(hasAction(appVerifyAction))
    }

    @Test
    fun anInvalidVerifyRequest_shouldNotLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<CommCareActivity>(invalidIntentRequest.apply {
            action = commcareVerifyAction
            putExtra(verifyGuidField.key(), verifyGuidField.value())
        })
        
        intended(CoreMatchers.not(hasAction(appVerifyAction)), Intents.times(2))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}
