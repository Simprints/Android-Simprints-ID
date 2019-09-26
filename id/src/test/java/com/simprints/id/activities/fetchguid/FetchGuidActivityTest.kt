package com.simprints.id.activities.fetchguid

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.common.livedata.testObserver
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.common.syntax.wheneverOnSuspend
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FetchGuidViewModelTest {

    @Mock private  lateinit var personRepository: PersonRepository
    @Mock private lateinit var simNetworkUtils: SimNetworkUtils
    @Mock private lateinit var sessionEventsManager: SessionEventsManager
    @Mock private lateinit var timeHelper: TimeHelper

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
    fun fetchGUIDsucceedsFromLocal_shouldReturnCorrectPersonSource() {
        wheneverOnSuspend(personRepository) { loadFromRemoteIfNeeded(anyNotNull(), anyNotNull()) } thenOnBlockingReturn PersonFetchResult(PeopleGeneratorUtils.getRandomPerson(), PersonFetchResult.PersonSource.LOCAL)

        val viewModel = FetchGuidViewModel(personRepository, simNetworkUtils, sessionEventsManager, timeHelper)
        viewModel.fetchGuid("projectId", "verifyGuid")

        val testObserver = viewModel.personFetch.testObserver()

        assertThat(testObserver.observedValues).contains(PersonFetchResult.PersonSource.LOCAL)
    }
}
