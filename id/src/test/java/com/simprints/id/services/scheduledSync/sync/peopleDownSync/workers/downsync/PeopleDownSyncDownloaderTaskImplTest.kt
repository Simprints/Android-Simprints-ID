package com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.downsync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.db.people_sync.down.DownSyncScopeRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.tools.TimeHelper
import io.mockk.mockk
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PeopleDownSyncDownloaderTaskImplTest {

    private val personLocalDataSource: PersonLocalDataSource = mockk()
    private val personRemoteDataSource: PersonRemoteDataSource = mockk()
    private val downSyncScopeRepository: DownSyncScopeRepository = mockk()
    private val timeHelper: TimeHelper = mockk()

    lateinit var downSyncTask: DownSyncTaskImpl
    @Before
    fun setUp() {
        downSyncTask = DownSyncTaskImpl(personLocalDataSource, personRemoteDataSource, downSyncScopeRepository, timeHelper)
    }
}
