package com.simprints.id.sync

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.SyncApiInterface
import com.simprints.id.data.db.sync.model.PatientsCount
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
class SimApiMock(private val delegate: BehaviorDelegate<SyncApiInterface>) : SyncApiInterface {

    override fun upSync(patientsJson: HashMap<String, ArrayList<fb_Person>>): Completable {
        return delegate.returning(buildSuccessResponseWith("")).upSync(patientsJson)
    }

    override fun downSync(date: Long, syncParams: Map<String, String>, batchSize: Int): Single<ResponseBody> {
        return delegate.returning(buildSuccessResponseWith("")).downSync(date, syncParams)
    }

    override fun getPatient(patientId: String, projectId: String): Single<ArrayList<fb_Person>> {
        return delegate.returning(buildSuccessResponseWith("")).getPatient(patientId, projectId)
    }

    override fun patientsCount(syncParams: Map<String, String>): Single<PatientsCount> {
        return delegate.returning(buildSuccessResponseWith("{\"patientsCount\": 10}")).patientsCount(mapOf())
    }

    private fun <T> buildSuccessResponseWith(body: T?): Call<T> {
        return Calls.response(Response.success(body))
    }
}

fun createMockServiceToFailRequests(retrofit: Retrofit): SyncApiInterface {
    return SimApiMock(createMockBehaviorService(retrofit, 0, SyncApiInterface::class.java))
}
