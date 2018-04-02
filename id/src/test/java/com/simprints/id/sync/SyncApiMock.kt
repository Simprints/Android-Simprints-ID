package com.simprints.id.sync

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.RemoteApiInterface
import com.simprints.id.data.db.sync.model.PersonsCount
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
class SimApiMock(private val delegate: BehaviorDelegate<RemoteApiInterface>) : RemoteApiInterface {

    override fun uploadPersons(patientsJson: HashMap<String, ArrayList<fb_Person>>): Completable {
        return delegate.returning(buildSuccessResponseWith("")).uploadPersons(patientsJson)
    }

    override fun downSync(date: Long, syncParams: Map<String, String>, batchSize: Int): Single<ResponseBody> {
        return delegate.returning(buildSuccessResponseWith("")).downSync(date, syncParams)
    }

    override fun downloadPersons(patientId: String, projectId: String): Single<ArrayList<fb_Person>> {
        return delegate.returning(buildSuccessResponseWith("")).downloadPersons(patientId, projectId)
    }

    override fun personsCount(syncParams: Map<String, String>): Single<PersonsCount> {
        return delegate.returning(buildSuccessResponseWith("{\"personsCount\": 10}")).personsCount(mapOf())
    }

    private fun <T> buildSuccessResponseWith(body: T?): Call<T> {
        return Calls.response(Response.success(body))
    }
}

fun createMockServiceToFailRequests(retrofit: Retrofit): RemoteApiInterface {
    return SimApiMock(createMockBehaviorService(retrofit, 0, RemoteApiInterface::class.java))
}
