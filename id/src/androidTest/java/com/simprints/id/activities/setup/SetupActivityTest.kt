package com.simprints.id.activities.setup

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.simprints.id.Application
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupActivityTest {

    @RelaxedMockK lateinit var mockSessionRepository: SessionRepository
    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val appModule by lazy {
        TestAppModule(app,
            sessionEventsManagerRule = DependencyRule.ReplaceRule {
                mockSessionRepository
            }
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        app.component = AndroidTestConfig(this, appModule = appModule)
            .componentBuilder()
            .build()
    }

    @Test
    fun launchSetupActivityWithLocationPermissions_shouldAddLocationToSession() {
        ActivityScenario.launch(SetupActivity::class.java)

        coVerify(exactly = 1) { mockSessionRepository.updateCurrentSession(any())}
    }
}
