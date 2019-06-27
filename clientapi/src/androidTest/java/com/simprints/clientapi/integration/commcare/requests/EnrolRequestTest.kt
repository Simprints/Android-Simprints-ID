package com.simprints.clientapi.integration.commcare.requests

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.di.KoinInjector.Companion.loadClientApiKoinModules
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
@MediumTest
class EnrolRequestTest : KoinTest {

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
        ActivityScenario.launch<CommCareActivity>(baseIntentRequest.apply { action = commcareEnrolAction })

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
        ActivityScenario.launch<CommCareActivity>(suspiciousIntentRequest.apply { action = commcareEnrolAction })
        intended(hasAction(appEnrolAction))
    }

    @Test
    fun anInvalidEnrolRequest_shouldNotLaunchAnAppEnrolRequest() {
        ActivityScenario.launch<CommCareActivity>(invalidIntentRequest.apply { action = commcareEnrolAction })
        intended(not(hasAction(appEnrolAction)), times(2))
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}
