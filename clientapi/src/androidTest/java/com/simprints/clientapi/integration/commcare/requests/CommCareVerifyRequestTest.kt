package com.simprints.clientapi.integration.commcare.requests

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.di.KoinInjector
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.commcare.commCareBaseIntentRequest
import com.simprints.clientapi.integration.commcare.commCareInvalidIntentRequest
import com.simprints.clientapi.integration.commcare.commCareSuspiciousIntentRequest
import com.simprints.clientapi.integration.commcare.commcareVerifyAction
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
class CommCareVerifyRequestTest : KoinTest {

    private val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)

    @Before
    fun setUp() {
        Intents.init()
        Intents.intending(hasAction(appVerifyAction)).respondWith(intentResultOk)

        loadClientApiKoinModules()
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    @Test
    fun aVerifyRequest_shouldLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply {
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
        ActivityScenario.launch<CommCareActivity>(commCareSuspiciousIntentRequest.apply {
            action = commcareVerifyAction
            putExtra(verifyGuidField.key(), verifyGuidField.value()) 
        })
        
        intended(hasAction(appVerifyAction))
    }

    @Test
    fun anInvalidVerifyRequest_shouldNotLaunchAnAppVerifyRequest() {
        ActivityScenario.launch<CommCareActivity>(commCareInvalidIntentRequest.apply {
            action = commcareVerifyAction
            putExtra(verifyGuidField.key(), verifyGuidField.value())
        })
        
        intended(CoreMatchers.not(hasAction(appVerifyAction)), Intents.times(2))
    }

    @After
    fun tearDown() {
        Intents.release()
        KoinInjector.unloadClientApiKoinModules()
    }
}
