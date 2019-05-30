package com.simprints.clientapi.integration.commcare

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.di.KoinInjector.Companion.loadClientApiKoinModules
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class EnrolRequestTest : KoinTest {

    private val actionEnrolApp = "com.simprints.clientapp.REGISTER"
    private val actionEnrol = "com.simprints.commcare.REGISTER"
    private val projectIdField = "projectId" to "some_project"
    private val userIdField = "userId" to "some_user_id"
    private val moduleIdField = "moduleId" to "some_module_id"
    private val metadataField = "metadata" to "some_metadata"
    private val extraField = "extra" to "some_extra"

    private val packageName = ApplicationProvider.getApplicationContext<Application>().packageName
    private val commCareActivityName = CommCareActivity::class.qualifiedName!!

    private val validRequest = Intent().apply {
        setClassName(packageName, commCareActivityName)
        action = actionEnrol
        putExtra(projectIdField.first, projectIdField.second)
        putExtra(userIdField.first, userIdField.second)
        putExtra(moduleIdField.first, moduleIdField.second)
    }

    private val invalidRequest = Intent().apply {
        setClassName(packageName, commCareActivityName)
        action = actionEnrol
        putExtra(projectIdField.first + "_wrong", projectIdField.second)
        putExtra(userIdField.first, userIdField.second)
        putExtra(moduleIdField.first, moduleIdField.second)
    }

    private val suspiciousRequest = Intent().apply {
        setClassName(packageName, commCareActivityName)
        action = actionEnrol
        putExtra(projectIdField.first, projectIdField.second)
        putExtra(userIdField.first, userIdField.second)
        putExtra(moduleIdField.first, moduleIdField.second)
        putExtra(extraField.first, extraField.second)
    }

    @Before
    fun setUp() {
        Intents.init()

        val sessionMock = Mockito.mock(SessionEvents::class.java)
        Mockito.`when`(sessionMock.id).thenReturn("")

        val sessionEventsManagerMock = Mockito.mock(SessionEventsManager::class.java)
        val fakeSession = Single.just(sessionMock)
        Mockito.`when`(sessionEventsManagerMock.createSession(anyNotNull())).thenReturn(fakeSession)
        Mockito.`when`(sessionEventsManagerMock.addEvent(anyNotNull())).thenReturn(Completable.complete())

        loadClientApiKoinModules()
        declare {
            factory { sessionEventsManagerMock }
        }
    }

    @Test
    fun aValidRequest_shouldGenerateAnEnrolResponseAndCalloutEvent() {
        ActivityScenario.launch<CommCareActivity>(validRequest)

        intended(IntentMatchers.hasAction(actionEnrolApp))
        intended(IntentMatchers.hasExtra(IAppRequest.BUNDLE_KEY, AppEnrollRequest(
            projectIdField.value(),
            userIdField.value(),
            moduleIdField.value(),
            metadataField.value())))
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Parcelize
    private data class AppEnrollRequest(
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        override val metadata: String
    ) : IAppEnrollRequest
}

fun Pair<String, String>.key() = first
fun Pair<String, String>.value() = second

