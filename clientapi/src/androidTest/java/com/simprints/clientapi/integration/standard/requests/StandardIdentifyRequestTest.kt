//package com.simprints.clientapi.integration.standard.requests
//
//import android.app.Activity
//import android.app.Instrumentation
//import androidx.test.core.app.ActivityScenario
//import androidx.test.espresso.intent.Intents
//import androidx.test.espresso.intent.Intents.intended
//import androidx.test.espresso.intent.Intents.times
//import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
//import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
//import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.simprints.clientapi.activities.commcare.CommCareActivity
//import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
//import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
//import com.simprints.clientapi.integration.*
//import com.simprints.clientapi.integration.standard.standardBaseIntentRequest
//import com.simprints.clientapi.integration.standard.standardIdentifyAction
//import com.simprints.clientapi.integration.standard.standardInvalidIntentRequest
//import com.simprints.clientapi.integration.standard.standardSuspiciousIntentRequest
//import com.simprints.moduleapi.app.requests.IAppRequest
//import com.simprints.testtools.android.bundleDataMatcherForParcelable
//import com.simprints.testtools.common.syntax.value
//import org.hamcrest.CoreMatchers
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.koin.test.KoinTest
//import org.koin.test.mock.declare
//
//@RunWith(AndroidJUnit4::class)
//class StandardIdentifyRequestTest : KoinTest {
//
//    private val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
//
//    @Before
//    fun setUp() {
//        Intents.init()
//        Intents.intending(hasAction(appIdentifyAction)).respondWith(intentResultOk)
//
//        loadClientApiKoinModules()
//        declare {
//            factory { buildDummySessionEventsManagerMock() }
//        }
//    }
//
//    @Test
//    fun anIdentifyRequest_shouldLaunchAnAppIdentifyRequest() {
//        ActivityScenario.launch<CommCareActivity>(standardBaseIntentRequest.apply { action = standardIdentifyAction })
//
//        val expectedAppRequest = AppIdentifyRequest(
//            projectIdField.value(),
//            userIdField.value(),
//            moduleIdField.value(),
//            metadataField.value())
//
//        intended(hasAction(appIdentifyAction))
//        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, bundleDataMatcherForParcelable(expectedAppRequest))))
//    }
//
//    @Test
//    fun aSuspiciousIdentifyRequest_shouldLaunchAnAppIdentifyRequest() {
//        ActivityScenario.launch<CommCareActivity>(standardSuspiciousIntentRequest.apply { action = standardIdentifyAction })
//        intended(hasAction(appIdentifyAction))
//    }
//
//    @Test
//    fun anInvalidIdentifyRequest_shouldNotLaunchAnAppIdentifyRequest() {
//        ActivityScenario.launch<CommCareActivity>(standardInvalidIntentRequest.apply { action = standardIdentifyAction })
//        intended(CoreMatchers.not(hasAction(appIdentifyAction)), times(2))
//    }
//
//    @After
//    fun tearDown() {
//        Intents.release()
//        unloadClientApiKoinModules()
//    }
//}
