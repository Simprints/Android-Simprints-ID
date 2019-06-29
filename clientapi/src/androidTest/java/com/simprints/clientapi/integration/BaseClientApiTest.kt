package com.simprints.clientapi.integration

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.simprints.clientapi.di.KoinInjector
import com.simprints.moduleapi.app.responses.IAppResponse
import org.junit.After
import org.junit.Before
import org.koin.test.KoinTest
import org.koin.test.mock.declare

abstract class BaseClientApiTest : KoinTest {

    @Before
    open fun setUp() {
        Intents.init()

        KoinInjector.loadClientApiKoinModules()
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    protected fun mockAppModulResponse(appResponse: IAppResponse,
                                              action: String) {

        val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().apply {
            this.putExtra(IAppResponse.BUNDLE_KEY, appResponse)
        })
        Intents.intending(IntentMatchers.hasAction(action)).respondWith(intentResultOk)
    }

    @After
    fun tearDown() {
        Intents.release()
        KoinInjector.unloadClientApiKoinModules()
    }
}
