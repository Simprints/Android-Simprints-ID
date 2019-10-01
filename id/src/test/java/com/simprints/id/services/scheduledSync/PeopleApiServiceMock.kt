package com.simprints.id.services.scheduledSync

import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.ApiModes
import com.simprints.id.data.db.person.remote.models.ApiPeopleCount
import com.simprints.id.data.db.person.remote.models.ApiPostPerson
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.PipeSeparatorWrapperForURLListParam
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

    override fun uploadPeople(projectId: String, patientsJson: HashMap<String, List<ApiPostPerson>>): Single<Result<Void?>> {
        return delegate.returning(buildSuccessResponseWith("")).uploadPeople(projectId, patientsJson)
    }

    override fun downSync(projectId: String, userId: String?, moduleId: String?, lastKnownPatientId: String?, lastKnownPatientUpdatedAt: Long?, modes: PipeSeparatorWrapperForURLListParam<ApiModes>): Single<ResponseBody> {
        return delegate.returning(buildSuccessResponseWith("")).downSync(projectId, userId, moduleId, lastKnownPatientId, lastKnownPatientUpdatedAt)
    }

    override fun requestPerson(patientId: String, projectId: String, modes: PipeSeparatorWrapperForURLListParam<ApiModes>): Single<Response<ApiGetPerson>> {
        return delegate.returning(buildSuccessResponseWith("")).requestPerson(patientId, projectId)
    }

    override fun requestPeopleCount(projectId: String, userId: String?, moduleId: PipeSeparatorWrapperForURLListParam<String>?, modes: PipeSeparatorWrapperForURLListParam<ApiModes>): Single<Response<List<ApiPeopleCount>>> {
        return delegate.returning(buildSuccessResponseWith("[{\"projectId\":\"project-321\",\"moduleId\":\"module1\",\"userId\":\"Bob\",\"modes\":[\"FINGERPRINT\",\"FACE\"],\"count\":3141}, {\"projectId\":\"project-321\",\"moduleId\":\"module2\",\"userId\":\"Bob\",\"modes\":[\"FINGERPRINT\",\"FACE\"],\"count\":3132}]"))
            .requestPeopleCount(projectId, userId, moduleId, PipeSeparatorWrapperForURLListParam(ApiModes.FINGERPRINT, ApiModes.FACE))
    }

    private fun <T> buildSuccessResponseWith(body: T?): Call<T> {
        return Calls.response(Response.success(body))
    }
}
