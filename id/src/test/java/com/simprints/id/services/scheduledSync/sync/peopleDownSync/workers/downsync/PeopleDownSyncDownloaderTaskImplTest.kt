package com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.downsync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTaskImpl
import com.simprints.id.tools.TimeHelper
import io.mockk.mockk
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PeopleDownSyncDownloaderTaskImplTest {

    private val personLocalDataSource: PersonLocalDataSource = mockk()
    private val personRemoteDataSource: PersonRemoteDataSource = mockk()
    private val downSyncScopeRepository: PeopleDownSyncScopeRepository = mockk()
    private val timeHelper: TimeHelper = mockk()

    private lateinit var downSyncTask: PeopleDownSyncDownloaderTaskImpl
    @Before
    fun setUp() {
        downSyncTask = PeopleDownSyncDownloaderTaskImpl(personLocalDataSource, personRemoteDataSource, downSyncScopeRepository, timeHelper)
    }
}
