package com.simprints.id.activities.fetchguid

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.exceptions.unexpected.DownloadingAPersonWhoDoesntExistOnServerException
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.common.livedata.testObserver
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.common.syntax.wheneverOnSuspend
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import retrofit2.HttpException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class FetchGuidViewModelTest {

    @Mock private  lateinit var personRepository: PersonRepository
    @Mock private lateinit var simNetworkUtils: SimNetworkUtils
    @Mock private lateinit var sessionEventsManager: SessionEventsManager
    @Mock private lateinit var timeHelper: TimeHelper

    companion object {
        private const val PROJECT_ID = "project_id"
        private const val VERIFY_GUID = "verify_guid"
    }

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .rescheduleRxMainThread()
            .coroutinesMainThread()

        MockitoAnnotations.initMocks(this)
        configureMocks()
    }

    private fun configureMocks() {
        whenever(sessionEventsManager) { addEventInBackground(anyNotNull()) } thenDoNothing {}
    }

    @Test
    fun fetchGuidSucceedsFromLocal_shouldReturnCorrectPersonSource() {
        wheneverOnSuspend(personRepository) { loadFromRemoteIfNeeded(anyNotNull(), anyNotNull()) } thenOnBlockingReturn PersonFetchResult(PeopleGeneratorUtils.getRandomPerson(), PersonFetchResult.PersonSource.LOCAL)

        val viewModel = FetchGuidViewModel(personRepository, simNetworkUtils, sessionEventsManager, timeHelper)
        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val testObserver = viewModel.personFetch.testObserver()

        assertThat(testObserver.observedValues).contains(PersonFetchResult.PersonSource.LOCAL)
    }

    @Test
    fun fetchGuidSucceedsFromRemote_shouldReturnCorrectPersonSource() {
        wheneverOnSuspend(personRepository) { loadFromRemoteIfNeeded(anyNotNull(), anyNotNull()) } thenOnBlockingReturn PersonFetchResult(PeopleGeneratorUtils.getRandomPerson(), PersonFetchResult.PersonSource.REMOTE)

        val viewModel = FetchGuidViewModel(personRepository, simNetworkUtils, sessionEventsManager, timeHelper)
        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val testObserver = viewModel.personFetch.testObserver()

        assertThat(testObserver.observedValues).contains(PersonFetchResult.PersonSource.REMOTE)
    }

    @Test
    fun fetchGuidFailsFromLocalOffline_shouldReturnFailedOfflinePersonSource() {
        whenever(simNetworkUtils) { isConnected() } thenReturn false
        wheneverOnSuspend(personRepository) { loadFromRemoteIfNeeded(anyNotNull(), anyNotNull()) } thenOnBlockingThrow HttpException::class.java

        val viewModel = FetchGuidViewModel(personRepository, simNetworkUtils, sessionEventsManager, timeHelper)
        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val testObserver = viewModel.personFetch.testObserver()

        assertThat(testObserver.observedValues).contains(PersonFetchResult.PersonSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR)
    }

    @Test
    fun fetchGuidFailsFromLocalAndRemoteOnline_shouldReturnNotFoundPersonSource() {
        whenever(simNetworkUtils) { isConnected() } thenReturn true
        wheneverOnSuspend(personRepository) { loadFromRemoteIfNeeded(anyNotNull(), anyNotNull()) } thenOnBlockingThrow DownloadingAPersonWhoDoesntExistOnServerException::class.java

        val viewModel = FetchGuidViewModel(personRepository, simNetworkUtils, sessionEventsManager, timeHelper)
        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val testObserver = viewModel.personFetch.testObserver()

        assertThat(testObserver.observedValues).contains(PersonFetchResult.PersonSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
    }
}
