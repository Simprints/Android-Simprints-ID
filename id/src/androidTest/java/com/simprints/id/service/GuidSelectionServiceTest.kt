package com.simprints.id.service

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.simprints.id.Application
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.commontesttools.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.models.domain.events.GuidSelectionEvent
import com.simprints.id.data.analytics.eventData.models.local.RlSession
import com.simprints.id.data.analytics.eventData.models.local.toDomainSession
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.testSnippets.launchActivityEnrol
import com.simprints.id.testSnippets.setupLoginInfoToBeSignedIn
import com.simprints.id.testSnippets.setupRandomGeneratorToGenerateKey
import com.simprints.id.testtools.ActivityUtils
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.RandomGenerator
import com.simprints.libsimprints.SimHelper
import com.simprints.testframework.android.tryOnUiUntilTimeout
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class GuidSelectionServiceTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val module by lazy {
        TestAppModule(app,
            randomGeneratorRule = DependencyRule.MockRule,
            sessionEventsManagerRule = DependencyRule.SpyRule,
            analyticsManagerRule = DependencyRule.MockRule,
            loginInfoManagerRule = DependencyRule.SpyRule)
    }

    private val realmForDataEvent
        get() = (realmSessionEventsManager as RealmSessionEventsDbManagerImpl).getRealmInstance().blockingGet()

    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var sessionEventsManagerSpy: SessionEventsManager
    @Inject lateinit var loginInfoManagerSpy: LoginInfoManager
    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager

    @Rule @JvmField val scanTestRule = ActivityUtils.checkLoginFromIntentActivityTestRule()

    @Before
    fun setUp() {
        AndroidTestConfig(this, module).fullSetupWith {
            setupRandomGeneratorToGenerateKey(DefaultTestConstants.DEFAULT_REALM_KEY, randomGeneratorMock)
            setupLoginInfoToBeSignedIn(loginInfoManagerSpy, DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId, DEFAULT_TEST_CALLOUT_CREDENTIALS.userId)
        }
    }

    @Test
    fun testWithStartedService() {
        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, scanTestRule)
        var session = sessionEventsManagerSpy.createSession().blockingGet()

        sessionEventsManagerSpy.updateSession {
            it.projectId = loginInfoManagerSpy.getSignedInProjectIdOrEmpty()
        }.blockingGet()

        val simHelper = SimHelper(DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId, DEFAULT_TEST_CALLOUT_CREDENTIALS.userId)
        simHelper.confirmIdentity(app, session.id, "some_guid_confirmed")

        tryOnUiUntilTimeout(10000, 500) {
            realmForDataEvent.refresh()
            session = realmForDataEvent.where(RlSession::class.java).equalTo("id", session.id).findFirst()?.toDomainSession()
            val potentialGuidSelectionEvent = session.events.filterIsInstance(GuidSelectionEvent::class.java).first()
            Assert.assertNotNull(potentialGuidSelectionEvent)
            Assert.assertEquals(potentialGuidSelectionEvent.selectedId, "some_guid_confirmed")
        }
    }
}
