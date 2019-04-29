package com.simprints.id.services

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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

@RunWith(AndroidJUnit4::class)
@LargeTest
class GuidSelectionServiceAndroidTest {

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

        val intent = Intent("com.simprints.clientapp.CONFIRM_IDENTITY")
        intent.putExtra(IAppConfirmation.BUNDLE_KEY,
            AppIdentifyConfirmation(
                DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId,
                session.id,
                "some_guid_confirmed"))
        intent.setPackage(Constants.SIMPRINTS_PACKAGE_NAME)

        ApplicationProvider.getApplicationContext<Application>().startService(intent)

        tryOnSystemUntilTimeout(10000, 500) {
            realmForDataEvent.refresh()
            session = realmForDataEvent.where(DbSession::class.java).equalTo("id", session.id).findFirst()?.toDomainSession()
            val potentialGuidSelectionEvent = session.events.filterIsInstance(GuidSelectionEvent::class.java).firstOrNull()
            Assert.assertNotNull(potentialGuidSelectionEvent)
            Assert.assertEquals(potentialGuidSelectionEvent?.selectedId, "some_guid_confirmed")
        }
    }

    @Parcelize
    data class AppIdentifyConfirmation(
        override val projectId: String,
        override val sessionId: String,
        override val selectedGuid: String
    ) : IAppIdentifyConfirmation
}
