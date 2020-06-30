package com.simprints.clientapi.integration

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Intent
import android.os.Parcelable
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.simprints.clientapi.di.KoinInjector
import com.simprints.id.data.db.event.SessionRepository
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.moduleapi.app.responses.IAppResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.After
import org.junit.Before
import org.koin.test.KoinTest
import org.koin.test.mock.declareModule

open class BaseClientApiTest : KoinTest {

    internal val projectIdField = "projectId" to "some_project"
    internal val userIdField = "userId" to "some_user_id"
    internal val moduleIdField = "moduleId" to "some_module_id"
    internal val metadataField = "metadata" to "some_metadata"
    internal val verifyGuidField = "verifyGuid" to "8b3f577c-b6c7-4677-9af2-b08cd7f71b79"
    internal val sessionIdField = "sessionId" to "some_sessionid"
    internal val selectedGuidField = "selectedGuid" to "8b3f577c-b6c7-4677-9af2-b08cd7f71b79"
    private val extraField = "extra" to "some_extra"
    internal val packageName = ApplicationProvider.getApplicationContext<Application>().packageName

    internal val baseIntentRequest = Intent().apply {
        putExtra(projectIdField.key(), projectIdField.value())
        putExtra(userIdField.key(), userIdField.value())
        putExtra(metadataField.key(), metadataField.value())
    }

    internal open fun getInvalidIntentRequest(baseIntent: Intent = baseIntentRequest,
                                              invalidPair: Pair<String, String> = projectIdField) =  baseIntent.apply {
        putExtra("${invalidPair.key()}_wrong", invalidPair.value())
        removeExtra(invalidPair.key())
    }

    internal open fun makeIntentRequestSuspicious(baseIntent: Intent = baseIntentRequest) =  baseIntent.apply {
        putExtra( extraField.key(),  extraField.value())
    }

    @Before
    open fun setUp() {
        Intents.init()

        KoinInjector.loadClientApiKoinModules()
        declareModule {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    private fun buildDummySessionEventsManagerMock(): SessionRepository {
        val sessionMock = mockk<SessionCaptureEvent>(relaxed = true)
        every { sessionMock.id } returns ""
        val repo = mockk<SessionRepository>(relaxed = true)
        coEvery { repo.getCurrentSession() } returns sessionMock
        return repo
    }

    protected fun mockAppModuleResponse(appResponse: IAppResponse,
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

    companion object {
        internal const val RETURN_FOR_FLOW_COMPLETED = true
        internal const val RETURN_FOR_FLOW_NOT_COMPLETED = !RETURN_FOR_FLOW_COMPLETED
        internal const val APP_ENROL_ACTION = "com.simprints.clientapp.REGISTER"
        internal const val APP_ENROL_LAST_BIOMETRICS_ACTION = "com.simprints.clientapp.REGISTER_LAST_BIOMETRICS"
        internal const val APP_IDENTIFY_ACTION = "com.simprints.clientapp.IDENTIFY"
        internal const val APP_VERIFICATION_ACTION = "com.simprints.clientapp.VERIFY"
        internal const val APP_CONFIRM_ACTION = "com.simprints.clientapp.CONFIRM_IDENTITY"
    }

    fun <T : Parcelable> bundleDataMatcherForParcelable(parcelable: T) =
        object : BaseMatcher<T>() {
            override fun describeTo(description: Description?) {}
            override fun matches(item: Any?): Boolean {
                return item.toString() == parcelable.toString()
            }
        }
}
