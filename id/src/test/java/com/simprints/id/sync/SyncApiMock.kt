package com.simprints.id.sync

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.DownSyncParams
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.sync.models.PeopleCount
import com.simprints.id.testUtils.retrofit.createMockBehaviorService
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls

// It's required to use NetworkBehavior, even if response is not used in the tests (e.g failing responses due to no connectivity).
// To mock response (code, body, type) use FakeResponseInterceptor for okHttpClient
class SimApiMock(private val delegate: BehaviorDelegate<PeopleRemoteInterface>) : PeopleRemoteInterface {

    override fun uploadPeople(patientsJson: HashMap<String, ArrayList<fb_Person>>): Completable {
        return delegate.returning(buildSuccessResponseWith("")).uploadPeople(patientsJson)
    }

    override fun downSync(projectId: String, syncParams: DownSyncParams, batchSize: Int): Single<ResponseBody> {
        return delegate.returning(buildSuccessResponseWith("")).downSync("", syncParams)
    }

    override fun person(patientId: String, projectId: String): Single<fb_Person> {
        return delegate.returning(buildSuccessResponseWith("")).person(patientId, projectId)
    }

    override fun peopleCount(syncParams: Map<String, String>): Single<PeopleCount> {
        return delegate.returning(buildSuccessResponseWith("{\"count\": 10}")).peopleCount(mapOf())
    }

    private fun <T> buildSuccessResponseWith(body: T?): Call<T> {
        return Calls.response(Response.success(body))
    }
}

fun createMockServiceToFailRequests(retrofit: Retrofit): PeopleRemoteInterface {
    return SimApiMock(createMockBehaviorService(retrofit, 0, PeopleRemoteInterface::class.java))
}
