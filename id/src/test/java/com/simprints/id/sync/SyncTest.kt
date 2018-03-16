package com.simprints.id.sync

import com.google.gson.Gson
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.sync.NaiveSync
import com.simprints.id.data.db.sync.SimApi
import com.simprints.id.data.db.sync.SimApiInterface
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.testUtils.whenever
import com.simprints.id.tools.JsonHelper
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.createMockBehaviorService
import com.simprints.id.tools.roboletric.TestApplication
import com.simprints.id.tools.utils.FirestoreMigationUtils.getRandomPeople
import com.simprints.libcommon.Progress
import com.simprints.libcommon.UploadProgress
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class SyncTest : RxJavaTest() {

    @Before
    fun setUp() {
    }

    @Test
    fun uploadPeopleInBatches_shouldWorkWithPoorConnection() {

        val localDbManager = Mockito.mock(LocalDbManager::class.java)
        val patientsToUpload = getRandomPeople(35)
        whenever(localDbManager.getPeopleToUpSync()).thenReturn(patientsToUpload)

        val sync = NaiveSyncTest(
            SimApiMock(createMockBehaviorService(SimApi().retrofit, 50, SimApiInterface::class.java)),
            null,
            localDbManager,
            JsonHelper.create())

        val testObserver = sync.uploadNewPatients({ false }, 10).test()
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValueSequence(arrayListOf(
                UploadProgress(10, patientsToUpload.size),
                UploadProgress(20, patientsToUpload.size),
                UploadProgress(30, patientsToUpload.size),
                UploadProgress(35, patientsToUpload.size)))
    }

    @Test
    fun uploadPeopleGetInterrupted_shouldStopUploading() {

        val localDbManager = Mockito.mock(LocalDbManager::class.java)
        val patientsToUpload = getRandomPeople(35)
        whenever(localDbManager.getPeopleToUpSync()).thenReturn(patientsToUpload)

        val sync = NaiveSyncTest(
            SimApiMock(createMockBehaviorService(SimApi().retrofit, 50, SimApiInterface::class.java)),
            null,
            localDbManager,
            JsonHelper.create())

        val count = AtomicInteger(0)
        val testObserver = sync.uploadNewPatients({ count.addAndGet(1) > 2 }, 10).test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValueCount(1)
    }

    @Test
    fun uploadSingleBatchOfPeople_shouldWorkWithPoorConnection() {

        val sync = NaiveSyncTest(
            SimApiMock(createMockBehaviorService(SimApi().retrofit, 50, SimApiInterface::class.java)),
            null,
            Mockito.mock(LocalDbManager::class.java),
            JsonHelper.create())

        val patients = getRandomPeople(3)
        val testObserver = sync.makeUploadRequest(ArrayList(patients.map { fb_Person(it) })).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValue { it == patients.size }
    }
}

class NaiveSyncTest(api: SimApiInterface,
                    realmConfig: RealmConfiguration?,
                    localDbManager: LocalDbManager,
                    gson: Gson) : NaiveSync(api, realmConfig, localDbManager, gson) {

    public override fun makeUploadRequest(patientsToUpload: ArrayList<fb_Person>): Single<Int> {
        return super.makeUploadRequest(patientsToUpload)
    }

    public override fun uploadNewPatients(isInterrupted: () -> Boolean, batchSize: Int): Observable<Progress> {
        return super.uploadNewPatients(isInterrupted, batchSize)
    }
}
