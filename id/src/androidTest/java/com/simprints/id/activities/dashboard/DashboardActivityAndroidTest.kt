package com.simprints.id.activities.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.di.TestAppModule
import com.simprints.testtools.android.waitOnUi
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardActivityAndroidTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var mockViewModel: DashboardViewModel

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val appModule by lazy {
        TestAppModule(app)
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        AndroidTestConfig(
            appModule = appModule,
            viewModelModule = buildViewModelModule()
        ).initComponent().testAppComponent.inject(this)
    }

    @Test
    fun withConsentRequiredEnabled_shouldShowPrivacyNoticeMenuItem() {
        ActivityScenario.launch(DashboardActivity::class.java)

        every { mockViewModel.consentRequiredLiveData } returns MutableLiveData<Boolean>(true)

        openActionBarOverflowOrOptionsMenu(app.applicationContext)

        assertDisplayed("Privacy Notice")
    }

    @Test
    fun withOnlyCommCareAsSyncLocation_shouldNotTriggerSyncAndNotShowSyncCard() {
        every { mockViewModel.syncToBFSIDAllowed } returns MutableLiveData<Boolean>(false)

        ActivityScenario.launch(DashboardActivity::class.java)

        waitOnUi(1000)

        coVerify(exactly = 0) { mockViewModel.syncIfRequired() }

        onView(withId(R.id.dashboard_sync_card)).check(matches(not(isDisplayed())))
    }

    @Test
    fun withSimprintsAsSyncLocation_shouldTriggerSyncAndShowSyncCard() {
        every { mockViewModel.syncToBFSIDAllowed } returns MutableLiveData<Boolean>(true)

        val mockSyncStateLiveData = MutableLiveData<DashboardSyncCardState>()

        every { mockViewModel.syncCardStateLiveData } returns mockSyncStateLiveData

        coEvery { mockViewModel.syncIfRequired() } answers {
            val connectingState = DashboardSyncCardState.SyncConnecting(null, 0, null)
            mockSyncStateLiveData.postValue(connectingState)
        }

        ActivityScenario.launch(DashboardActivity::class.java)

        waitOnUi(1000)

        coVerify(exactly = 1) { mockViewModel.syncIfRequired() }

        onView(withId(R.id.dashboard_sync_card)).check(matches(isDisplayed()))
    }
}
