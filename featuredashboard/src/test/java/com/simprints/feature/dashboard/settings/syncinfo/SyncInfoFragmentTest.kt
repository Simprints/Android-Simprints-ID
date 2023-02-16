package com.simprints.feature.dashboard.settings.syncinfo

import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.eventsystem.events_sync.models.EventSyncState
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.tools.launchFragmentInHiltContainer
import com.simprints.feature.dashboard.tools.testNavController
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.UpSynchronizationKind.*
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class SyncInfoFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<SyncInfoViewModel>(relaxed = true)

    @Test
    fun `should display the number of total record count if records in local is not null`() {
        mockRecordsInLocal(10)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.totalRecordsCount)).check(matches(withText("10")))
        onView(withId(R.id.totalRecordsProgress)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the progress bar for total record count if records in local is null`() {
        mockRecordsInLocal(null)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.totalRecordsCount)).check(matches(not(isDisplayed())))
        onView(withId(R.id.totalRecordsProgress)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display the number of record to upload if records to up sync is not null`() {
        mockRecordsToUpSync(10)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToUploadCount)).check(matches(withText("10")))
        onView(withId(R.id.recordsToUploadProgress)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the progress bar for record to upload if records to up sync is null`() {
        mockRecordsToUpSync(null)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToUploadCount)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recordsToUploadProgress)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display the number of image to upload if images to up sync is not null`() {
        mockImagesToUpload(10)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.imagesToUploadCount)).check(matches(withText("10")))
        onView(withId(R.id.imagesToUploadProgress)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the progress bar for image to upload if images to up sync is null`() {
        mockImagesToUpload(null)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.imagesToUploadCount)).check(matches(not(isDisplayed())))
        onView(withId(R.id.imagesToUploadProgress)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display the number of record to download if records to down sync is not null`() {
        mockRecordsToDownSync(10)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToDownloadCount)).check(matches(withText("10")))
        onView(withId(R.id.recordsToDownloadProgress)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the progress bar for record to download if records to down sync is null`() {
        mockRecordsToDownSync(null)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToDownloadCount)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recordsToDownloadProgress)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display the number of record to delete if records to delete is not null`() {
        mockRecordsToDelete(10)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToDeleteCount)).check(matches(withText("10")))
        onView(withId(R.id.recordsToDeleteProgress)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the progress bar for record to delete if records to delete is null`() {
        mockRecordsToDelete(null)

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToDeleteCount)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recordsToDeleteProgress)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display the module selection if the partition is Module and the module options is not empty`() {
        every { viewModel.isModuleSyncAndModuleIdOptionsNotEmpty(any()) } returns true

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.moduleSelectionButton)).check(matches(isDisplayed()))
        onView(withId(R.id.modulesTabHost)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun `should hide the module selection if the partition is not Module`() {
        mockConfiguration(mockk {
            every { synchronization } returns mockk(relaxed = true) {
                every { down } returns DownSynchronizationConfiguration(
                    DownSynchronizationConfiguration.PartitionType.PROJECT,
                    3,
                    listOf("module1")
                )
            }
        })

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.moduleSelectionButton)).check(matches(not(isDisplayed())))
        onView(withId(R.id.modulesTabHost)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should hide the module selection if the module options is not empty`() {
        mockConfiguration(mockk {
            every { synchronization } returns mockk(relaxed = true) {
                every { down } returns DownSynchronizationConfiguration(
                    DownSynchronizationConfiguration.PartitionType.MODULE,
                    3,
                    listOf()
                )
            }
        })

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.moduleSelectionButton)).check(matches(not(isDisplayed())))
        onView(withId(R.id.modulesTabHost)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the records to download and delete if the down sync is enabled`() {
        mockConfiguration(mockk {
            every { synchronization } returns mockk(relaxed = true) {
                every { frequency } returns SynchronizationConfiguration.Frequency.PERIODICALLY
            }
        })

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToDownloadCardView)).check(matches(isDisplayed()))
        onView(withId(R.id.recordsToDeleteCardView)).check(matches(isDisplayed()))
    }

    @Test
    fun `should hide the records to download and delete if the down sync is disabled`() {
        mockConfiguration(mockk {
            every { synchronization } returns mockk(relaxed = true) {
                every { frequency } returns SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
            }
        })

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToDownloadCardView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recordsToDeleteCardView)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the images and records to upload if the up sync is enabled`() {
        mockConfiguration(mockk {
            every { synchronization } returns mockk(relaxed = true) {
                every { up } returns mockk {
                    every { simprints } returns SimprintsUpSynchronizationConfiguration(ALL)
                }
            }
        })

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToUploadCardView)).check(matches(isDisplayed()))
        onView(withId(R.id.imagesToUploadCardView)).check(matches(isDisplayed()))
    }

    @Test
    fun `should hide the images and records to upload if the up sync is disabled`() {
        mockConfiguration(mockk {
            every { synchronization } returns mockk(relaxed = true) {
                every { up } returns mockk {
                    every { simprints } returns SimprintsUpSynchronizationConfiguration(NONE)
                }
            }
        })

        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.recordsToUploadCardView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.imagesToUploadCardView)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should refresh the information when clicking on the menu icon`() {
        launchFragmentInHiltContainer<SyncInfoFragment>()

        onView(withId(R.id.sync_redo)).perform(click())
        verify(exactly = 2) { viewModel.refreshInformation() }
    }

    @Test
    fun `should navigate back when clicking on the back icon`() {
        val navController = mockk<NavController>(relaxed = true)
        launchFragmentInHiltContainer<SyncInfoFragment>(navController = navController)

        onView(withContentDescription("back")).perform(click())

        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should navigate to the module selection when clicking on the module selection button`() {
        val navController = testNavController(R.navigation.graph_dashboard, R.id.syncInfoFragment)

        launchFragmentInHiltContainer<SyncInfoFragment>(navController = navController)

        onView(withId(R.id.moduleSelectionButton)).perform(click())

        Truth.assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.moduleSelectionFragment)
    }

    @Test
    fun `should fetch the sync information when the last sync state is updated`() {
        val eventSyncState = EventSyncState("id", 0, 10, listOf(), listOf())
        mockLastSyncState(eventSyncState)
        mockIsConnection(true)
        mockConfiguration(mockk {
            every { synchronization } returns mockk(relaxed = true) {
                every { down } returns DownSynchronizationConfiguration(DownSynchronizationConfiguration.PartitionType.USER, 0, emptyList())
            }
        })

        launchFragmentInHiltContainer<SyncInfoFragment>()

        verify(exactly = 1) { viewModel.fetchSyncInformationIfNeeded(eventSyncState) }
    }

    private fun mockRecordsInLocal(number: Int?) {
        every { viewModel.recordsInLocal } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Int?>>().onChanged(number)
            }
        }
    }

    private fun mockRecordsToUpSync(number: Int?) {
        every { viewModel.recordsToUpSync } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Int?>>().onChanged(number)
            }
        }
    }

    private fun mockImagesToUpload(number: Int?) {
        every { viewModel.imagesToUpload } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Int?>>().onChanged(number)
            }
        }
    }

    private fun mockRecordsToDownSync(number: Int?) {
        every { viewModel.recordsToDownSync } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Int?>>().onChanged(number)
            }
        }
    }

    private fun mockRecordsToDelete(number: Int?) {
        every { viewModel.recordsToDelete } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<Int?>>().onChanged(number)
            }
        }
    }

    private fun mockConfiguration(configuration: ProjectConfiguration) {
        every { viewModel.configuration } returns mockk {
            every { value } returns configuration
            every { observe(any(), any()) } answers {
                secondArg<Observer<ProjectConfiguration>>().onChanged(configuration)
            }
        }
    }

    private fun mockLastSyncState(state: EventSyncState) {
        every { viewModel.lastSyncState } returns mockk {
            every { value } returns state
            every { observe(any(), any()) } answers {
                secondArg<Observer<EventSyncState>>().onChanged(state)
            }
        }
    }

    private fun mockIsConnection(isConnected: Boolean) {
        every { viewModel.isConnected } returns mockk {
            every { value } returns isConnected
            every { observe(any(), any()) } answers {
                secondArg<Observer<Boolean>>().onChanged(isConnected)
            }
        }
    }
}
