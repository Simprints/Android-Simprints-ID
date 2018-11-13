package com.simprints.id.service

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.domain.events.GuidSelectionEvent
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DependencyRule
import com.simprints.id.testSnippets.launchActivityEnrol
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libsimprints.SimHelper
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import com.simprints.id.testSnippets.*
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class GuidSelectionServiceTest : DaggerForAndroidTests() {

    @Inject lateinit var sessionEventsManagerSpy: SessionEventsManager
    @Inject lateinit var loginInfoManagerSpy: LoginInfoManager

    val scanTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    val calloutCredentials: CalloutCredentials = CalloutCredentials(
        "00000002-0000-0000-0000-000000000000",
        "the_one_and_only_module",
        "the_lone_user")

    override var module by lazyVar {
        AppModuleForAndroidTests(
            app,
            sessionEventsManagerRule = DependencyRule.SpyRule,
            analyticsManagerRule = DependencyRule.MockRule,
            loginInfoManagerRule = DependencyRule.MockRule)
    }

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super.setUp()
        testAppComponent.inject(this)

        setupLoginInfoToBeSignedIn(loginInfoManagerSpy, calloutCredentials.projectId, calloutCredentials.userId)

        app.initDependencies()
    }

    @Test
    fun testWithStartedService() {
        launchActivityEnrol(calloutCredentials, scanTestRule)
        var session = sessionEventsManagerSpy.createSession().blockingGet()

        sessionEventsManagerSpy.updateSession({
            session.projectId = loginInfoManagerSpy.getSignedInProjectIdOrEmpty()
        }).blockingGet()

        val simHelper = SimHelper(calloutCredentials.projectId, calloutCredentials.userId)
        simHelper.confirmIdentity(app, session.id, "some_guid_confirmed")

        Awaitility.await().atMost(30, TimeUnit.SECONDS).pollDelay(Duration.TWO_SECONDS).until<Any> {
            val potentialSessionWithGUIDEvent = sessionEventsManagerSpy.getCurrentSession().blockingGet()
            potentialSessionWithGUIDEvent.events.findLast { it is GuidSelectionEvent } != null
        }

        session = sessionEventsManagerSpy.getCurrentSession().blockingGet()
        val potentialGuidSelectionEvent = session.events.findLast { it is GuidSelectionEvent } as GuidSelectionEvent?
        Assert.assertNotNull(potentialGuidSelectionEvent)
        Assert.assertEquals(potentialGuidSelectionEvent?.selectedId, "some_guid_confirmed")
    }
}
