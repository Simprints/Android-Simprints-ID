package com.simprints.id.activities.setup

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.common.truth.Truth
import com.simprints.core.domain.modality.Modality
import com.simprints.id.Application
import com.simprints.id.orchestrator.steps.core.requests.SetupPermission
import com.simprints.id.orchestrator.steps.core.requests.SetupRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.di.TestAppModule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupActivityTest {

    @MockK
    lateinit var mockSplitInstallManager: SplitInstallManager

    @MockK
    lateinit var mockkWorkManager: WorkManager

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val appModule by lazy {
        TestAppModule(app)
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(SplitInstallManagerFactory::class)
        every { mockSplitInstallManager.installedModules } returns setOf("fingerprint", "face")

        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns mockkWorkManager
        every { SplitInstallManagerFactory.create(any()) } returns mockSplitInstallManager


        app.component = AndroidTestConfig(appModule = appModule)
            .componentBuilder()
            .build()
    }

    @Test
    fun launchSetupActivityWithLocationPermissions_shouldFinishWithSuccess() {

        val request =
            SetupRequest(listOf(Modality.FINGER, Modality.FACE), listOf(SetupPermission.LOCATION))
        val intent = Intent().apply {
            setClassName(
                ApplicationProvider.getApplicationContext<android.app.Application>().packageName,
                SetupActivity::class.qualifiedName!!
            )
            putExtra(CoreResponse.CORE_STEP_BUNDLE, request)
        }

        val activityScenario = ActivityScenario.launch<SetupActivity>(intent)

        coVerify(exactly = 1) { mockkWorkManager.enqueue(any<WorkRequest>()) }
        Truth.assertThat(activityScenario.result.resultCode).isEqualTo(RESULT_OK)

    }
}
