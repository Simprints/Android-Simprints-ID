package com.simprints.id.activities.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.displayed
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.commontesttools.di.TestViewModelModule
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.android.waitOnUi
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test

class DashboardActivityAndroidTest {

    @MockK
    lateinit var mockPreferencesManager: SettingsPreferencesManager

    @MockK
    lateinit var mockViewModelFactory: DashboardViewModelFactory

    @MockK
    lateinit var mockViewModel: DashboardViewModel

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val preferencesModule by lazy {
        TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.ReplaceRule {
            mockPreferencesManager
        })
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every {
            mockViewModelFactory.create<DashboardViewModel>(any())
        } returns mockViewModel

        app.component = AndroidTestConfig(this, preferencesModule = preferencesModule)
            .componentBuilder()
            .viewModelModule(buildViewModelModule())
            .build()
    }

    @Test
    fun withConsentRequiredDisabled_shouldNotShowPrivacyNoticeMenuItem() {
        ActivityScenario.launch(DashboardActivity::class.java)

        every { mockPreferencesManager.consentRequired } returns false

        openActionBarOverflowOrOptionsMenu(app.applicationContext)

        onView(withText("Privacy Notice")).check(doesNotExist())
    }

    @Test
    fun withConsentRequiredEnabled_shouldShowPrivacyNoticeMenuItem() {
        ActivityScenario.launch(DashboardActivity::class.java)

        every { mockPreferencesManager.consentRequired } returns true

        openActionBarOverflowOrOptionsMenu(app.applicationContext)

        displayed {
            text("Privacy Notice")
        }
    }

    @Test
    fun withOnlyCommCareAsSyncLocation_shouldNotTriggerSyncAndNotShowSyncCard() {

        every { mockPreferencesManager.syncDestinationSettings } returns
            listOf(SyncDestinationSetting.COMMCARE)

        ActivityScenario.launch(DashboardActivity::class.java)

        waitOnUi(1000)

        coVerify(exactly = 0) {  mockViewModel.syncIfRequired() }

        onView(withId(R.id.dashboard_sync_card)).check(matches(not(isDisplayed())))
    }

    @Test
    fun withSimprintsAsSyncLocation_shouldTriggerSyncAndShowSyncCard() {
        every { mockPreferencesManager.syncDestinationSettings } returns
            listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.SIMPRINTS)

        val mockSyncStateLiveData = MutableLiveData<DashboardSyncCardState>()

        every { mockViewModel.syncCardStateLiveData } returns mockSyncStateLiveData

        coEvery { mockViewModel.syncIfRequired() } answers  {
            val connectingState = DashboardSyncCardState.SyncConnecting(null, 0, null)
            mockSyncStateLiveData.postValue(connectingState)
        }

        ActivityScenario.launch(DashboardActivity::class.java)

        waitOnUi(1000)

        coVerify(exactly = 1) {  mockViewModel.syncIfRequired() }

        onView(withId(R.id.dashboard_sync_card)).check(matches(isDisplayed()))
    }

    private fun buildViewModelModule() = TestViewModelModule(
        dashboardViewModelFactoryRule = DependencyRule.ReplaceRule {
            mockViewModelFactory
        }
    )
}
