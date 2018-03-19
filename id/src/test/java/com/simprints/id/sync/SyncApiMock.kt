package com.simprints.id.sync

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.SyncApiInterface
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.tools.retrofit.createMockBehaviorService
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

    override fun upSync(key: String, patientsJson: String): Completable {
        return delegate.returning(buildSuccessResponseWith("")).upSync(anyNotNull(), anyNotNull())
    }

    override fun downSync(key: String, date: Long, syncParams: Map<String, String>, batchSize: Int): Single<ResponseBody> {
        return delegate.returning(buildSuccessResponseWith("")).downSync(anyNotNull(), anyNotNull(), anyNotNull())
    }

    override fun getPatient(key: String, patientId: String): Single<fb_Person> {
        return delegate.returning(buildSuccessResponseWith("")).getPatient(anyNotNull(), anyNotNull())
    }

    override fun patientsCount(key: String, syncParams: Map<String, String>): Single<Int> {
        return delegate.returning(buildSuccessResponseWith("")).patientsCount(anyNotNull(), mapOf())
    }

    private fun <T> buildSuccessResponseWith(body: T?): Call<T> {
        return Calls.response(Response.success(body))
    }
}

fun createMockServiceToFailRequests(retrofit: Retrofit): SyncApiInterface {
    return SimApiMock(createMockBehaviorService(retrofit, 0, SyncApiInterface::class.java))
}
