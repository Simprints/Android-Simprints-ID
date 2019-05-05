package com.simprints.id.services

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ServiceTestRule
import com.simprints.id.Application
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.state.LoginStateMocker
import com.simprints.id.commontesttools.state.setupRandomGeneratorToGenerateKey
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.GuidSelectionEvent
import com.simprints.id.data.analytics.eventdata.models.local.DbSession
import com.simprints.id.data.analytics.eventdata.models.local.toDomainSession
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.RandomGenerator
import com.simprints.libsimprints.Constants
import com.simprints.moduleapi.app.requests.confirmations.IAppConfirmation
import com.simprints.moduleapi.app.requests.confirmations.IAppIdentifyConfirmation
import com.simprints.testtools.android.tryOnSystemUntilTimeout
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.mock
import kotlinx.android.parcel.Parcelize
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
@LargeTest
class GuidSelectionManagerTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val module by lazy {
        TestAppModule(app,
            randomGeneratorRule = DependencyRule.ReplaceRule { mock<RandomGenerator>().apply { setupRandomGeneratorToGenerateKey(this) } },
            sessionEventsManagerRule = DependencyRule.SpyRule,
            crashReportManagerRule = DependencyRule.MockRule,
            loginInfoManagerRule = DependencyRule.SpyRule)
    }

    private val realmForDataEvent
        get() = (realmSessionEventsManager as RealmSessionEventsDbManagerImpl).getRealmInstance().blockingGet()

    @Inject lateinit var sessionEventsManagerSpy: SessionEventsManager
    @Inject lateinit var loginInfoManagerSpy: LoginInfoManager
    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    @Inject lateinit var guidSelectionManager: GuidSelectionManager

    @Before
    fun setUp() {
        AndroidTestConfig(this, module).fullSetup()
        LoginStateMocker.setupLoginInfoToBeSignedIn(loginInfoManagerSpy, DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId, DEFAULT_TEST_CALLOUT_CREDENTIALS.userId)
    }

    @Test
    fun testWithStartedService() {
        var session = sessionEventsManagerSpy.createSession("some_app_version_name").blockingGet()

        sessionEventsManagerSpy.updateSession {
            it.projectId = loginInfoManagerSpy.getSignedInProjectIdOrEmpty()
        }.blockingGet()

        val request = AppIdentityConfirmationRequest(
            DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId,
            session.id,
            "some_guid_confirmed")

        guidSelectionManager
            .saveGUIDSelection(request)
            .test()
            .awaitAndAssertSuccess()

        realmForDataEvent.refresh()
        session = realmForDataEvent.where(DbSession::class.java).equalTo("id", session.id).findFirst()?.toDomainSession()
        val potentialGuidSelectionEvent = session.events.filterIsInstance(GuidSelectionEvent::class.java).firstOrNull()
        Assert.assertNotNull(potentialGuidSelectionEvent)
        Assert.assertEquals(potentialGuidSelectionEvent?.selectedId, "some_guid_confirmed")
    }
}
