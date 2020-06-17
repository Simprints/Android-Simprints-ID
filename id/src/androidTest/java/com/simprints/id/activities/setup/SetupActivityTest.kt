package com.simprints.id.activities.setup

import android.content.Intent
import android.location.Location
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.simprints.id.Application
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.domain.moduleapi.core.requests.SetupPermission
import com.simprints.id.domain.moduleapi.core.requests.SetupRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.LocationManager
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupActivityTest {

    companion object {
        private const val PROVIDER = "flp"
        private const val LAT = 37.377166
        private const val LNG = -122.086966
        private const val ACCURACY = 3.0f
    }

    @RelaxedMockK lateinit var mockSessionRepository: SessionRepository
    @RelaxedMockK lateinit var mockLocationManager: LocationManager

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val appModule by lazy {
        TestAppModule(app,
            sessionEventsManagerRule = DependencyRule.ReplaceRule {
                mockSessionRepository
            },
            locationManagerRule = DependencyRule.ReplaceRule {
                mockLocationManager
            }
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { mockLocationManager.requestLocation(any()) } returns flowOf(listOf(buildFakeLocation()))

        app.component = AndroidTestConfig(this, appModule = appModule)
            .componentBuilder()
            .build()
    }

    @Test
    fun launchSetupActivityWithLocationPermissions_shouldAddLocationToSession() {
        val request = SetupRequest(listOf(SetupPermission.LOCATION))
        val intent = Intent().apply {
            setClassName(ApplicationProvider.getApplicationContext<android.app.Application>().packageName,
                SetupActivity::class.qualifiedName!!)
            putExtra(CoreResponse.CORE_STEP_BUNDLE, request)
        }

        ActivityScenario.launch<SetupActivity>(intent)

        coVerify(exactly = 1) { mockSessionRepository.updateCurrentSession(any())}
    }

    private fun buildFakeLocation() = Location(PROVIDER).apply {
        longitude = LNG
        latitude = LAT
        accuracy = ACCURACY
    }
}
