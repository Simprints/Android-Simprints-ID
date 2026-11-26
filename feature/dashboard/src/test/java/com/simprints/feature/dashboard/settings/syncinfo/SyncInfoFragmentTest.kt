package com.simprints.feature.dashboard.settings.syncinfo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.*
import androidx.test.core.app.ApplicationProvider.*
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.*
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.logout.sync.LogoutSyncFragment
import com.simprints.feature.dashboard.logout.sync.LogoutSyncUiState
import com.simprints.feature.dashboard.logout.sync.LogoutSyncViewModel
import com.simprints.feature.dashboard.main.MainFragment
import com.simprints.feature.dashboard.main.MainUiState
import com.simprints.feature.dashboard.main.MainViewModel
import com.simprints.feature.dashboard.main.dailyactivity.DailyActivityUiState
import com.simprints.feature.dashboard.main.dailyactivity.DailyActivityViewModel
import com.simprints.feature.dashboard.main.projectdetails.ProjectDetailsUiState
import com.simprints.feature.dashboard.main.projectdetails.ProjectDetailsViewModel
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.feature.dashboard.tools.MviStubs
import com.simprints.feature.dashboard.tools.seedNavigationResult
import com.simprints.feature.dashboard.tools.stubMvi
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginParams
import com.simprints.feature.login.LoginResult
import com.simprints.feature.moduleselector.ModuleSelectorContract
import com.simprints.feature.moduleselector.ModuleSelectorResult
import com.simprints.infra.uibase.navigation.toBundle
import com.simprints.testtools.hilt.HiltTestActivity
import com.simprints.testtools.hilt.launchFragmentInHiltContainer
import com.simprints.testtools.hilt.testNavController
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.flow.update
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import com.google.android.material.R as MR
import com.simprints.feature.login.R as LoginR
import com.simprints.feature.module.selector.R as ModuleSelectorR
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
internal class SyncInfoFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val syncInfoViewModel = mockk<SyncInfoViewModel>(relaxed = true)

    // Host ViewModels, so the main card and the logout panel can be launched inside their real hosts
    @BindValue
    @JvmField
    internal val mainViewModel = mockk<MainViewModel>(relaxed = true)

    @BindValue
    @JvmField
    internal val projectDetailsViewModel = mockk<ProjectDetailsViewModel>(relaxed = true)

    @BindValue
    @JvmField
    internal val dailyActivityViewModel = mockk<DailyActivityViewModel>(relaxed = true)

    @BindValue
    @JvmField
    internal val logoutSyncViewModel = mockk<LogoutSyncViewModel>(relaxed = true)

    private lateinit var mvi: MviStubs<SyncInfoUiState, SyncInfoUiEffect>

    private val context: Context = getApplicationContext()

    private lateinit var hostActivity: FragmentActivity
    private lateinit var launchedFragment: Fragment

    @Before
    fun setUp() {
        hiltRule.inject()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())
        mainViewModel.stubMvi(MainUiState())
        projectDetailsViewModel.stubMvi(ProjectDetailsUiState())
        dailyActivityViewModel.stubMvi(DailyActivityUiState())
        logoutSyncViewModel.stubMvi(LogoutSyncUiState())
        mvi = syncInfoViewModel.stubMvi(SyncInfoUiState())
    }

    // S-6: result routing per host

    @Test
    fun `standalone confirmed module selection result is passed to the view model`() {
        val navController = navController(Host.STANDALONE)
        navController.seedNavigationResult(
            R.id.syncInfoFragment,
            ModuleSelectorContract.DESTINATION,
            ModuleSelectorResult(isConfirmed = true),
        )

        launch(Host.STANDALONE, navController)

        verify(
            exactly = 1,
        ) { syncInfoViewModel.onAction(SyncInfoUiAction.ModuleSelectionResultReceived(ModuleSelectorResult(isConfirmed = true))) }
    }

    // The confirmation check is the view model's (SyncInfoViewModelTest: module selection not confirmed does nothing)
    @Test
    fun `standalone not confirmed module selection result is passed to the view model`() {
        val navController = navController(Host.STANDALONE)
        navController.seedNavigationResult(
            R.id.syncInfoFragment,
            ModuleSelectorContract.DESTINATION,
            ModuleSelectorResult(isConfirmed = false),
        )

        launch(Host.STANDALONE, navController)

        verify(exactly = 1) {
            syncInfoViewModel.onAction(SyncInfoUiAction.ModuleSelectionResultReceived(ModuleSelectorResult(isConfirmed = false)))
        }
    }

    @Test
    fun `standalone login result is passed to the view model`() {
        val navController = navController(Host.STANDALONE)
        navController.seedNavigationResult(R.id.syncInfoFragment, LoginContract.DESTINATION, LoginResult(isSuccess = true))

        launch(Host.STANDALONE, navController)

        verify(exactly = 1) { syncInfoViewModel.onAction(SyncInfoUiAction.LoginResultReceived(LoginResult(isSuccess = true))) }
    }

    @Test
    fun `main card reads the module selection result from the main screen entry`() {
        val navController = navController(Host.MAIN)
        navController.seedNavigationResult(R.id.mainFragment, ModuleSelectorContract.DESTINATION, ModuleSelectorResult(isConfirmed = true))

        launch(Host.MAIN, navController)

        verify(
            exactly = 1,
        ) { syncInfoViewModel.onAction(SyncInfoUiAction.ModuleSelectionResultReceived(ModuleSelectorResult(isConfirmed = true))) }
    }

    @Test
    fun `logout panel reads the module selection result from the logout sync entry`() {
        val navController = navController(Host.LOGOUT)
        navController.seedNavigationResult(
            R.id.logOutSyncFragment,
            ModuleSelectorContract.DESTINATION,
            ModuleSelectorResult(isConfirmed = true),
        )

        launch(Host.LOGOUT, navController)

        // The host mode reaches the view model before the result, so the restart runs in pre-logout mode
        verifyOrder {
            syncInfoViewModel.onAction(SyncInfoUiAction.HostAttached(isPreLogoutUpSync = true))
            syncInfoViewModel.onAction(SyncInfoUiAction.ModuleSelectionResultReceived(ModuleSelectorResult(isConfirmed = true)))
        }
        verify(
            exactly = 1,
        ) { syncInfoViewModel.onAction(SyncInfoUiAction.ModuleSelectionResultReceived(ModuleSelectorResult(isConfirmed = true))) }
    }

    // S-7: host configuration

    @Test
    fun `standalone shows the toolbar and section headers and hides the status header`() {
        launch(Host.STANDALONE)

        render(fullyVisibleSyncInfo())

        syncView(Host.STANDALONE, R.id.appBarLayout).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.headerRecordSync).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.sectionDivider1).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.headerImageSync).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.sectionDivider2).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.headerModuleSelection).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.layoutImagesSync).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.selectedModulesView).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.syncStatusHeader).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.STANDALONE, R.id.sectionFooter).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.STANDALONE, R.id.layoutComboImageCounter).check(matches(withEffectiveVisibility(Visibility.GONE)))
        verify(exactly = 1) { syncInfoViewModel.onAction(SyncInfoUiAction.HostAttached(isPreLogoutUpSync = false)) }
        verify(exactly = 0) { syncInfoViewModel.onAction(SyncInfoUiAction.HostAttached(isPreLogoutUpSync = true)) }
    }

    @Test
    fun `main card shows the status header with the settings button and hides the toolbar and section headers`() {
        launch(Host.MAIN)

        render(fullyVisibleSyncInfo())

        syncView(Host.MAIN, R.id.syncStatusHeader).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.MAIN, R.id.syncSettingsButton).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.MAIN, R.id.sectionFooter).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.MAIN, R.id.appBarLayout).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.MAIN, R.id.headerRecordSync).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.MAIN, R.id.sectionDivider1).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.MAIN, R.id.sectionDivider2).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.MAIN, R.id.layoutImagesSync).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.MAIN, R.id.layoutComboImageCounter).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.MAIN, R.id.selectedModulesView).check(matches(withEffectiveVisibility(Visibility.GONE)))
        verify(exactly = 1) { syncInfoViewModel.onAction(SyncInfoUiAction.HostAttached(isPreLogoutUpSync = false)) }
        verify(exactly = 0) { syncInfoViewModel.onAction(SyncInfoUiAction.HostAttached(isPreLogoutUpSync = true)) }
    }

    // S-7, LV-9: the logout panel runs in pre-logout mode
    @Test
    fun `logout panel shows the combined image counter, hides the image section and runs in pre-logout mode`() {
        launch(Host.LOGOUT)

        render(fullyVisibleSyncInfo())

        syncView(Host.LOGOUT, R.id.syncStatusHeader).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.LOGOUT, R.id.layoutComboImageCounter).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.LOGOUT, R.id.sectionFooter).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.LOGOUT, R.id.syncSettingsButton).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.LOGOUT, R.id.appBarLayout).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.LOGOUT, R.id.headerRecordSync).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.LOGOUT, R.id.layoutImagesSync).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.LOGOUT, R.id.selectedModulesView).check(matches(withEffectiveVisibility(Visibility.GONE)))
        verify(exactly = 1) { syncInfoViewModel.onAction(SyncInfoUiAction.HostAttached(isPreLogoutUpSync = true)) }
        verify(exactly = 0) { syncInfoViewModel.onAction(SyncInfoUiAction.HostAttached(isPreLogoutUpSync = false)) }
    }

    // S-8: sync button and in-progress footer

    @Test
    fun `standalone sync button stays visible when the state hides it`() {
        launch(Host.STANDALONE)

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isSyncButtonVisible = false, isSyncButtonEnabled = false)))

        // The same state is rendered (the button is disabled, its XML default is enabled), and the button is still shown
        syncView(Host.STANDALONE, R.id.buttonSyncRecordsNow).check(matches(not(isEnabled())))
        syncView(Host.STANDALONE, R.id.buttonSyncRecordsNow).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun `logout panel sync button follows the state visibility`() {
        launch(Host.LOGOUT)

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isSyncButtonVisible = false)))
        syncView(Host.LOGOUT, R.id.buttonSyncRecordsNow).check(matches(withEffectiveVisibility(Visibility.GONE)))

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isSyncButtonVisible = true)))
        syncView(Host.LOGOUT, R.id.buttonSyncRecordsNow).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun `sync button text shows syncing, then try again, then sync records`() {
        launch(Host.STANDALONE)

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isProgressVisible = true, isSyncButtonForRetry = true)))
        syncView(Host.STANDALONE, R.id.buttonSyncRecordsNow).check(matches(withText(IDR.string.sync_info_button_records_syncing)))

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isSyncButtonForRetry = true)))
        syncView(Host.STANDALONE, R.id.buttonSyncRecordsNow).check(matches(withText(IDR.string.sync_info_button_try_again)))

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords()))
        syncView(Host.STANDALONE, R.id.buttonSyncRecordsNow).check(matches(withText(IDR.string.sync_info_button_sync_records)))
    }

    @Test
    fun `sync button enabled state follows the state`() {
        launch(Host.STANDALONE)

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isSyncButtonEnabled = false)))
        syncView(Host.STANDALONE, R.id.buttonSyncRecordsNow).check(matches(not(isEnabled())))

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isSyncButtonEnabled = true)))
        syncView(Host.STANDALONE, R.id.buttonSyncRecordsNow).check(matches(isEnabled()))
    }

    @Test
    fun `logout panel shows the sync in progress footer only while the state asks for it`() {
        launch(Host.LOGOUT)

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isFooterSyncInProgressVisible = true)))
        syncView(Host.LOGOUT, R.id.textFooterRecordSyncInProgress).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isFooterSyncInProgressVisible = false)))
        syncView(Host.LOGOUT, R.id.textFooterRecordSyncInProgress).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun `standalone never shows the sync in progress footer`() {
        launch(Host.STANDALONE)

        render(
            SyncInfo(
                syncInfoSectionRecords = SyncInfoSectionRecords(isFooterSyncInProgressVisible = true, isFooterReadyToLogOutVisible = true),
            ),
        )

        // The same state is rendered: the ready-to-log-out footer is shown
        syncView(Host.STANDALONE, R.id.layoutFooterRecordLoggingOut).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.textFooterRecordSyncInProgress).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // S-9: module section

    @Test
    fun `standalone shows the module section with its list when modules are selected and hides it when unavailable`() {
        launch(Host.STANDALONE)

        render(SyncInfo(syncInfoSectionModules = SyncInfoSectionModules(isSectionAvailable = true, moduleCounts = MODULE_COUNTS)))
        syncView(Host.STANDALONE, R.id.layoutModuleSelection).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.selectedModulesView).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        render(SyncInfo(syncInfoSectionModules = SyncInfoSectionModules(isSectionAvailable = false, moduleCounts = MODULE_COUNTS)))
        syncView(Host.STANDALONE, R.id.layoutModuleSelection).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun `main card shows the module section only while no module is selected`() {
        launch(Host.MAIN)

        render(SyncInfo(syncInfoSectionModules = SyncInfoSectionModules(isSectionAvailable = true, moduleCounts = emptyList())))
        syncView(Host.MAIN, R.id.layoutModuleSelection).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.MAIN, R.id.selectedModulesView).check(matches(withEffectiveVisibility(Visibility.GONE)))

        render(SyncInfo(syncInfoSectionModules = SyncInfoSectionModules(isSectionAvailable = true, moduleCounts = MODULE_COUNTS)))
        syncView(Host.MAIN, R.id.layoutModuleSelection).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun `logout panel shows the module section only while no module is selected`() {
        launch(Host.LOGOUT)

        render(SyncInfo(syncInfoSectionModules = SyncInfoSectionModules(isSectionAvailable = true, moduleCounts = emptyList())))
        syncView(Host.LOGOUT, R.id.layoutModuleSelection).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        render(SyncInfo(syncInfoSectionModules = SyncInfoSectionModules(isSectionAvailable = true, moduleCounts = MODULE_COUNTS)))
        syncView(Host.LOGOUT, R.id.layoutModuleSelection).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun `select modules button opens module selection`() {
        val navController = navController(Host.MAIN)
        launch(Host.MAIN, navController)
        render(SyncInfo(syncInfoSectionModules = SyncInfoSectionModules(isSectionAvailable = true, moduleCounts = emptyList())))

        syncView(Host.MAIN, R.id.buttonSelectModules).perform(scrollTo(), click())

        assertThat(navController.currentDestination?.id).isEqualTo(ModuleSelectorR.id.moduleSelectionFragment)
    }

    @Test
    fun `no modules instruction opens module selection`() {
        val navController = navController(Host.STANDALONE)
        launch(Host.STANDALONE, navController)
        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(recordSyncVisibleState = RecordSyncVisibleState.NO_MODULES_ERROR)))

        syncView(Host.STANDALONE, R.id.text_event_sync_instructions_no_modules).perform(scrollTo(), click())

        assertThat(navController.currentDestination?.id).isEqualTo(ModuleSelectorR.id.moduleSelectionFragment)
    }

    // S-10: error popup priority

    @Test
    fun `error popup shows too many requests before maintenance`() {
        launch(Host.STANDALONE)
        renderError(SyncInfoError(isBackendMaintenance = true, backendMaintenanceEstimatedOutage = 600, isTooManyRequests = true))

        clickErrorInstruction()

        onView(withText(IDR.string.sync_info_details_too_many_modules)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `error popup shows maintenance with the estimated outage when it is known`() {
        launch(Host.STANDALONE)
        renderError(SyncInfoError(isBackendMaintenance = true, backendMaintenanceEstimatedOutage = 600))

        clickErrorInstruction()

        val expected = context.getString(
            IDR.string.error_backend_maintenance_with_time_message,
            TimeUtils.getFormattedEstimatedOutage(600),
        )
        onView(withText(expected)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `error popup shows maintenance without a time when the outage is unknown`() {
        launch(Host.STANDALONE)
        renderError(SyncInfoError(isBackendMaintenance = true, backendMaintenanceEstimatedOutage = 0))

        clickErrorInstruction()

        onView(withText(IDR.string.error_backend_maintenance_message)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `error popup shows the generic error and follows the latest state`() {
        launch(Host.STANDALONE)
        renderError(SyncInfoError(isTooManyRequests = true))
        renderError(SyncInfoError())

        clickErrorInstruction()

        onView(withText(IDR.string.sync_info_details_error)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    // S-11: progress text and bar

    @Test
    fun `records progress shows one line per part with record and image names and sets the bar`() {
        launch(Host.STANDALONE)

        render(
            SyncInfo(
                syncInfoSectionRecords = SyncInfoSectionRecords(
                    isProgressVisible = true,
                    progress = SyncProgressInfo(
                        progressParts = listOf(PENDING_PART, DONE_PART),
                        progressBarPercentage = 42,
                    ),
                ),
            ),
        )
        val records = context.getString(IDR.string.sync_info_item_record_or_event)
        val images = context.getString(IDR.string.sync_info_item_image)
        syncView(Host.STANDALONE, R.id.textEventSyncProgress).check(
            matches(
                withText(
                    context.getString(IDR.string.sync_info_progress_pending, records) + "\n" +
                        context.getString(IDR.string.sync_info_progress_complete, images),
                ),
            ),
        )
        assertProgress(R.id.eventSyncProgressBar, 42)

        render(
            SyncInfo(
                syncInfoSectionRecords = SyncInfoSectionRecords(
                    isProgressVisible = true,
                    progress = SyncProgressInfo(
                        progressParts = listOf(ONGOING_WITHOUT_COUNTERS_PART, ONGOING_WITH_COUNTERS_PART),
                        progressBarPercentage = 70,
                    ),
                ),
            ),
        )
        syncView(Host.STANDALONE, R.id.textEventSyncProgress).check(
            matches(
                withText(
                    context.getString(IDR.string.sync_info_progress_ongoing_no_counters, records) + "\n" +
                        context.getString(IDR.string.sync_info_progress_ongoing, images, 3, 10),
                ),
            ),
        )
        assertProgress(R.id.eventSyncProgressBar, 70)
    }

    @Test
    fun `progress parts beyond the named items use the default item name`() {
        launch(Host.STANDALONE)

        render(
            SyncInfo(
                syncInfoSectionRecords = SyncInfoSectionRecords(
                    isProgressVisible = true,
                    progress = SyncProgressInfo(progressParts = listOf(DONE_PART, DONE_PART, PENDING_PART)),
                ),
            ),
        )

        val records = context.getString(IDR.string.sync_info_item_record_or_event)
        val images = context.getString(IDR.string.sync_info_item_image)
        val default = context.getString(IDR.string.sync_info_item_default)
        assertThat(default).isNotEqualTo(records)
        assertThat(default).isNotEqualTo(images)
        syncView(Host.STANDALONE, R.id.textEventSyncProgress).check(
            matches(
                withText(
                    context.getString(IDR.string.sync_info_progress_complete, records) + "\n" +
                        context.getString(IDR.string.sync_info_progress_complete, images) + "\n" +
                        context.getString(IDR.string.sync_info_progress_pending, default),
                ),
            ),
        )
    }

    @Test
    fun `image progress shows the image name and sets the bar`() {
        launch(Host.STANDALONE)

        render(
            SyncInfo(
                isImageSyncSectionVisible = true,
                syncInfoSectionImages = SyncInfoSectionImages(
                    isProgressVisible = true,
                    progress = SyncProgressInfo(progressParts = listOf(ONGOING_WITH_COUNTERS_PART), progressBarPercentage = 30),
                ),
            ),
        )

        val images = context.getString(IDR.string.sync_info_item_image)
        syncView(Host.STANDALONE, R.id.textImageSyncProgress).check(
            matches(withText(context.getString(IDR.string.sync_info_progress_ongoing, images, 3, 10))),
        )
        assertProgress(R.id.imageSyncProgressBar, 30)
    }

    // S-12: clicks

    @Test
    fun `sync now button asks the view model to sync records`() {
        launch(Host.STANDALONE)
        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(isSyncButtonEnabled = true)))

        syncView(Host.STANDALONE, R.id.buttonSyncRecordsNow).perform(scrollTo(), click())

        verify(exactly = 1) { syncInfoViewModel.onAction(SyncInfoUiAction.SyncRecordsClicked) }
    }

    @Test
    fun `image sync button asks the view model to toggle image sync`() {
        launch(Host.STANDALONE)
        render(SyncInfo(isImageSyncSectionVisible = true, syncInfoSectionImages = SyncInfoSectionImages(isSyncButtonEnabled = true)))

        syncView(Host.STANDALONE, R.id.buttonSyncImagesNow).perform(scrollTo(), click())

        verify(exactly = 1) { syncInfoViewModel.onAction(SyncInfoUiAction.SyncImagesClicked) }
    }

    @Test
    fun `main card settings button opens the sync info screen`() {
        val navController = navController(Host.MAIN)
        launch(Host.MAIN, navController)

        syncView(Host.MAIN, R.id.syncSettingsButton).perform(scrollTo(), click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.syncInfoFragment)
    }

    @Test
    fun `commcare permission instruction opens the app details settings`() {
        launch(Host.STANDALONE)
        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(recordSyncVisibleState = RecordSyncVisibleState.COMM_CARE_ERROR)))

        syncView(Host.STANDALONE, R.id.text_event_sync_instructions_comm_care_permission).perform(scrollTo(), click())

        val intent = shadowOf(hostActivity).nextStartedActivity
        assertThat(intent.action).isEqualTo(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        assertThat(intent.data).isEqualTo(Uri.fromParts("package", context.packageName, null))
    }

    @Test
    fun `records offline instruction opens the wireless settings`() {
        launch(Host.STANDALONE)
        render(SyncInfo(syncInfoSectionRecords = SyncInfoSectionRecords(recordSyncVisibleState = RecordSyncVisibleState.OFFLINE_ERROR)))

        syncView(Host.STANDALONE, R.id.text_event_sync_instructions_offline).perform(scrollTo(), click())

        assertWirelessSettingsStarted()
    }

    @Test
    fun `images offline instruction opens the wireless settings`() {
        launch(Host.STANDALONE)
        render(
            SyncInfo(
                isImageSyncSectionVisible = true,
                syncInfoSectionImages = SyncInfoSectionImages(isInstructionOfflineVisible = true),
            ),
        )

        syncView(Host.STANDALONE, R.id.textImageSyncInstructionsOffline).perform(scrollTo(), click())

        assertWirelessSettingsStarted()
    }

    @Test
    fun `toolbar up goes back to the previous screen`() {
        val navController = testNavController(R.navigation.graph_dashboard, R.id.settingsFragment).also {
            it.navigate(R.id.action_settingsFragment_to_syncInfoFragment)
        }
        launch(Host.STANDALONE, navController)

        onView(allOf(isAssignableFrom(ImageButton::class.java), withParent(withId(R.id.syncInfoToolbar)))).perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.settingsFragment)
    }

    // S-12, KI-1 fix: re-login navigation is a one-time effect, never replayed to a new view

    @Test
    fun `re-login button asks the view model for login`() {
        launch(Host.STANDALONE)
        render(SyncInfo(isLoginPromptSectionVisible = true))

        syncView(Host.STANDALONE, R.id.sync_relogin_required_login_button).perform(scrollTo(), click())

        verify(exactly = 1) { syncInfoViewModel.onAction(SyncInfoUiAction.RequestLogin) }
    }

    @Test
    fun `one login request navigates to login once with the login params`() {
        val navController = navController(Host.STANDALONE)
        launch(Host.STANDALONE, navController)

        sendLoginRequest()
        render(SyncInfo(isLoginPromptSectionVisible = true)) // an unrelated update does not navigate again

        assertLoginNavigations(navController, count = 1)
    }

    @Test
    fun `stopping and starting the same view does not navigate to login again`() {
        val navController = navController(Host.STANDALONE)
        launchStandaloneInScenario(navController).use { scenario ->
            val view = launchedFragment.requireView()
            sendLoginRequest()
            assertLoginNavigations(navController, count = 1)

            // Activity stop/start (app to background and back): the Fragment view is kept
            scenario.moveToState(Lifecycle.State.CREATED)
            assertThat(launchedFragment.viewLifecycleOwner.lifecycle.currentState).isEqualTo(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)

            assertThat(launchedFragment.requireView()).isSameInstanceAs(view)
            assertLoginNavigations(navController, count = 1)
        }
    }

    @Test
    fun `a login request sent while the view is stopped navigates once when it starts again`() {
        val navController = navController(Host.STANDALONE)
        launchStandaloneInScenario(navController).use { scenario ->
            scenario.moveToState(Lifecycle.State.CREATED)
            sendLoginRequest()
            assertLoginNavigations(navController, count = 0)

            scenario.moveToState(Lifecycle.State.RESUMED)
            shadowOf(Looper.getMainLooper()).idle()

            assertLoginNavigations(navController, count = 1)
        }
    }

    // KI-1 fix: before, the latest login request was replayed to every newly created view
    @Test
    fun `re-creating the view does not navigate to login again`() {
        val navController = navController(Host.STANDALONE)
        launch(Host.STANDALONE, navController)
        val firstView = launchedFragment.requireView()
        sendLoginRequest()
        assertLoginNavigations(navController, count = 1)

        recreateView()
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(launchedFragment.requireView()).isNotSameInstanceAs(firstView)
        assertLoginNavigations(navController, count = 1)
    }

    @Test
    fun `a second login request with equal params navigates again`() {
        val navController = navController(Host.STANDALONE)
        launch(Host.STANDALONE, navController)
        sendLoginRequest()
        assertLoginNavigations(navController, count = 1)

        sendLoginRequest()

        assertLoginNavigations(navController, count = 2)
    }

    // S-13: default render and image section

    @Test
    fun `before any value the default sync info is rendered`() {
        val navController = navController(Host.STANDALONE)
        launch(Host.STANDALONE, navController)

        syncView(Host.STANDALONE, R.id.totalRecordsProgress).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.recordsToUploadProgress).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.recordsToDownloadProgress).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        syncView(Host.STANDALONE, R.id.totalRecordsCount).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.STANDALONE, R.id.text_event_sync_instructions_default).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.STANDALONE, R.id.layoutImagesSync).check(matches(withEffectiveVisibility(Visibility.GONE)))
        syncView(Host.STANDALONE, R.id.layoutModuleSelection).check(matches(withEffectiveVisibility(Visibility.GONE)))
        // INVISIBLE, not GONE: the space stays
        syncView(Host.STANDALONE, R.id.progressConfigRefresh).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        assertLoginNavigations(navController, count = 0)
    }

    @Test
    fun `image sync button switches text and colour with image progress`() {
        launch(Host.STANDALONE)

        render(SyncInfo(isImageSyncSectionVisible = true, syncInfoSectionImages = SyncInfoSectionImages(isProgressVisible = true)))
        syncView(Host.STANDALONE, R.id.buttonSyncImagesNow).check(matches(withText(IDR.string.sync_info_button_images_sync_stop)))
        assertImageButtonColour(IDR.color.button_sync_images_background_red)

        render(SyncInfo(isImageSyncSectionVisible = true, syncInfoSectionImages = SyncInfoSectionImages(isProgressVisible = false)))
        syncView(Host.STANDALONE, R.id.buttonSyncImagesNow).check(matches(withText(IDR.string.sync_info_button_sync_images)))
        assertImageButtonColour(IDR.color.button_sync_images_background_default)
    }

    @Test
    fun `image section needs both the host flag and the state flag`() {
        launch(Host.STANDALONE)
        render(SyncInfo(isImageSyncSectionVisible = true))
        syncView(Host.STANDALONE, R.id.layoutImagesSync).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        render(SyncInfo(isImageSyncSectionVisible = false))
        syncView(Host.STANDALONE, R.id.layoutImagesSync).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    private enum class Host { STANDALONE, MAIN, LOGOUT }

    private fun navController(host: Host): TestNavHostController = when (host) {
        Host.STANDALONE -> testNavController(R.navigation.graph_dashboard, R.id.syncInfoFragment)
        Host.MAIN -> testNavController(R.navigation.graph_dashboard, R.id.mainFragment)
        Host.LOGOUT -> testNavController(R.navigation.graph_dashboard, R.id.aboutFragment).also {
            it.navigate(R.id.action_aboutFragment_to_logout_navigation)
        }
    }

    private fun launch(
        host: Host,
        navController: TestNavHostController = navController(host),
    ) {
        val onLaunched: Fragment.() -> Unit = {
            hostActivity = requireActivity()
            launchedFragment = this
        }
        when (host) {
            Host.STANDALONE -> launchFragmentInHiltContainer<SyncInfoFragment>(navController = navController, action = onLaunched)
            Host.MAIN -> launchFragmentInHiltContainer<MainFragment>(navController = navController, action = onLaunched)
            Host.LOGOUT -> launchFragmentInHiltContainer<LogoutSyncFragment>(navController = navController, action = onLaunched)
        }
    }

    // Like launchFragmentInHiltContainer, but keeps the scenario so the activity can be stopped and started without
    // destroying the Fragment view (HiltExt.moveToState(CREATED) caps the Fragment itself, which destroys its view).
    // The caller closes the returned scenario.
    private fun launchStandaloneInScenario(navController: TestNavHostController): ActivityScenario<HiltTestActivity> {
        val intent = Intent
            .makeMainActivity(ComponentName(context, HiltTestActivity::class.java))
            .putExtra(
                "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
                MR.style.Theme_MaterialComponents,
            )
        return ActivityScenario.launch<HiltTestActivity>(intent).onActivity { activity ->
            val fragment = SyncInfoFragment()
            fragment.viewLifecycleOwnerLiveData.observeForever { owner ->
                if (owner != null) Navigation.setViewNavController(fragment.requireView(), navController)
            }
            activity.supportFragmentManager.commitNow { add(android.R.id.content, fragment) }
            hostActivity = activity
            launchedFragment = fragment
        }
    }

    // Destroys and re-creates the view while the Fragment (and its ViewModel) stays, as when returning from the back stack.
    private fun recreateView() {
        launchedFragment.parentFragmentManager.commitNow { detach(launchedFragment) }
        launchedFragment.parentFragmentManager.commitNow { attach(launchedFragment) }
    }

    private fun syncView(
        host: Host,
        @IdRes id: Int,
    ): ViewInteraction = when (host) {
        Host.STANDALONE -> onView(withId(id))
        Host.MAIN, Host.LOGOUT -> onView(allOf(withId(id), isDescendantOfA(withId(R.id.dashboard_sync))))
    }

    // Keeps the rest of the state (host mode), as the view model does.
    private fun render(syncInfo: SyncInfo) {
        mvi.state.update { it.copy(syncInfo = syncInfo) }
    }

    private fun sendLoginRequest() {
        mvi.effects.trySend(SyncInfoUiEffect.NavigateToLogin(LOGIN_PARAMS))
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun renderError(error: SyncInfoError) = render(
        SyncInfo(
            syncInfoSectionRecords = SyncInfoSectionRecords(
                recordSyncVisibleState = RecordSyncVisibleState.ERROR,
                instructionPopupErrorInfo = error,
            ),
        ),
    )

    private fun clickErrorInstruction() {
        syncView(Host.STANDALONE, R.id.text_event_sync_instructions_error).perform(scrollTo(), click())
    }

    private fun fullyVisibleSyncInfo() = SyncInfo(
        isImageSyncSectionVisible = true,
        syncInfoSectionModules = SyncInfoSectionModules(isSectionAvailable = true, moduleCounts = emptyList()),
    )

    @Suppress("DEPRECATION")
    private fun assertLoginNavigations(
        navController: TestNavHostController,
        count: Int,
    ) {
        val loginEntries = navController.backStack.filter { it.destination.id == LoginR.id.loginFormFragment }
        assertThat(loginEntries).hasSize(count)
        val paramsKey = LOGIN_PARAMS.toBundle().keySet().single()
        loginEntries.forEach { assertThat(it.arguments?.getSerializable(paramsKey)).isEqualTo(LOGIN_PARAMS) }
    }

    private fun assertProgress(
        @IdRes progressBarId: Int,
        expected: Int,
    ) {
        syncView(Host.STANDALONE, progressBarId).check { view, noViewFoundException ->
            noViewFoundException?.let { throw it }
            assertThat((view as LinearProgressIndicator).progress).isEqualTo(expected)
        }
    }

    private fun assertImageButtonColour(colourRes: Int) {
        syncView(Host.STANDALONE, R.id.buttonSyncImagesNow).check { view: View, noViewFoundException ->
            noViewFoundException?.let { throw it }
            assertThat((view as MaterialButton).backgroundTintList?.defaultColor)
                .isEqualTo(ContextCompat.getColorStateList(context, colourRes)?.defaultColor)
        }
    }

    private fun assertWirelessSettingsStarted() {
        val intent: Intent = shadowOf(hostActivity).nextStartedActivity
        assertThat(intent.action).isEqualTo(Settings.ACTION_WIRELESS_SETTINGS)
    }

    private companion object {
        val LOGIN_PARAMS = LoginParams(projectId = "projectId", userId = TokenizableString.Raw("userId"))
        val MODULE_COUNTS = listOf(ModuleCount(name = "total", count = 3), ModuleCount(name = "module", count = 2))
        val PENDING_PART = SyncProgressInfoPart(isPending = true)
        val DONE_PART = SyncProgressInfoPart(isPending = false, isDone = true)
        val ONGOING_WITHOUT_COUNTERS_PART = SyncProgressInfoPart(isPending = false, areNumbersVisible = false)
        val ONGOING_WITH_COUNTERS_PART =
            SyncProgressInfoPart(isPending = false, areNumbersVisible = true, currentNumber = 3, totalNumber = 10)
    }
}
