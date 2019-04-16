package com.simprints.id.services.scheduledSync

import com.simprints.id.data.db.remote.models.PeopleCount
import com.simprints.id.data.db.remote.models.ApiPerson
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls

// It's required to use NetworkBehavior, even if response is not used in the tests (e.g failing modalityResponses due to no connectivity).
// To mock response (code, body, type) use FakeResponseInterceptor for okHttpClient
class PeopleApiServiceMock(private val delegate: BehaviorDelegate<PeopleRemoteInterface>) : PeopleRemoteInterface {

    override fun uploadPeople(projectId: String, patientsJson: HashMap<String, List<ApiPerson>>): Single<Result<Void?>> {
        return delegate.returning(buildSuccessResponseWith("")).uploadPeople(projectId, patientsJson)
    }

    override fun downSync(projectId: String, userId: String?, moduleId: String?, lastKnownPatientId: String?, lastKnownPatientUpdatedAt: Long?): Single<ResponseBody> {
        return delegate.returning(buildSuccessResponseWith("")).downSync(projectId, userId, moduleId, lastKnownPatientId, lastKnownPatientUpdatedAt)
    }

    override fun requestPerson(patientId: String, projectId: String): Single<Response<ApiPerson>> {
        return delegate.returning(buildSuccessResponseWith("")).requestPerson(patientId, projectId)
    }

    override fun requestPeopleCount(projectId: String, userId: String?, moduleId: String?): Single<Response<PeopleCount>> {
        return delegate.returning(buildSuccessResponseWith("{\"count\": 10}")).requestPeopleCount(projectId, userId, moduleId)
    }

    private fun <T> buildSuccessResponseWith(body: T?): Call<T> {
        return Calls.response(Response.success(body))
    }
}
