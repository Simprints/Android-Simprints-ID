package com.simprints.clientapi.integration.odk.requests

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.di.KoinInjector
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkEnrolAction
import com.simprints.clientapi.integration.odk.odkInvalidIntentRequest
import com.simprints.clientapi.integration.odk.odkSuspiciousIntentRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.android.bundleDataMatcherForParcelable
import com.simprints.testtools.common.syntax.value
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare


@RunWith(AndroidJUnit4::class)
class OdkEnrolRequestTest : KoinTest {

    private val intentResultOk = ActivityResult(Activity.RESULT_OK, null)

    @Before
    fun setUp() {
        Intents.init()
        intending(hasAction(appEnrolAction)).respondWith(intentResultOk)

        loadClientApiKoinModules()
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    @Test
    fun anEnrolRequest_shouldLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = odkEnrolAction })

        val expectedAppRequest = AppEnrollRequest(
            projectIdField.value(),
            userIdField.value(),
            moduleIdField.value(),
            metadataField.value())

        intended(hasAction(appEnrolAction))
        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
    }

    @Test
    fun aSuspiciousEnrolRequest_shouldLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(odkSuspiciousIntentRequest.apply { action = odkEnrolAction })
        intended(hasAction(appEnrolAction))
    }

    @Test
    fun anInvalidEnrolRequest_shouldNotLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<OdkActivity>(odkInvalidIntentRequest.apply { action = odkEnrolAction })
        intended(not(hasAction(appEnrolAction)), times(2))
    }

    @After
    fun tearDown() {
        Intents.release()
        KoinInjector.unloadClientApiKoinModules()
    }
}
