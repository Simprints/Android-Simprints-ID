package com.simprints.clientapi.integration

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.simprints.clientapi.di.KoinInjector
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.moduleapi.app.responses.IAppResponse
import com.simprints.testtools.common.syntax.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.koin.test.KoinTest
import org.koin.test.mock.declare

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

    internal val baseConfirmIntentRequest = Intent().apply {
        putExtra(projectIdField.key(), projectIdField.value())
        putExtra(sessionIdField.key(), sessionIdField.value())
        putExtra(selectedGuidField.key(), selectedGuidField.value())
    }

    internal val baseIntentRequest = Intent().apply {
        putExtra(projectIdField.key(), projectIdField.value())
        putExtra(userIdField.key(), userIdField.value())
        putExtra(moduleIdField.key(), moduleIdField.value())
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
        declare {
            factory { buildDummySessionEventsManagerMock() }
        }
    }

    private fun buildDummySessionEventsManagerMock(): SessionEventsManager =
        mock<SessionEventsManager>().apply {
            val sessionMock = mock<SessionEvents>().apply {
                whenever(this) { id } thenReturn ""
            }

            whenever(this) { createSession(anyNotNull()) } thenReturn Single.just(sessionMock)
            whenever(this) { addEvent(anyNotNull()) } thenReturn Completable.complete()
            whenever(this) { getCurrentSession() } thenReturn Single.just(sessionMock)
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
        internal const val APP_IDENTIFY_ACTION = "com.simprints.clientapp.IDENTIFY"
        internal const val APP_VERIFICATION_ACTION = "com.simprints.clientapp.VERIFY"
        internal const val APP_CONFIRM_ACTION = "com.simprints.clientapp.CONFIRM_IDENTITY"
    }
}
