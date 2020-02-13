package com.simprints.id.data.db.person.remote

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.remote.models.fromDomainToPostApi
import com.simprints.id.data.db.person.remote.models.fromGetApiToDomain
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiLastKnownPatient
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiPeopleOperationGroup
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiPeopleOperationWhereLabel
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiPeopleOperations
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.WhereLabelKey.*
import com.simprints.id.exceptions.safe.sync.EmptyPeopleOperationsParamsException
import com.simprints.id.tools.utils.retrySimNetworkCalls


open class PersonRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager,
                                      private val simApiClientFactory: SimApiClientFactory) : PersonRemoteDataSource {

    override suspend fun downloadPerson(patientId: String, projectId: String): Person =
        makeNetworkRequest({
            it.requestPerson(patientId, projectId).fromGetApiToDomain()
        }, "downloadPerson")

    override suspend fun uploadPeople(projectId: String, patientsToUpload: List<Person>) =
        makeNetworkRequest({
            it.uploadPeople(projectId, hashMapOf("patients" to patientsToUpload.map(Person::fromDomainToPostApi)))
        }, "uploadPatientBatch")


    override suspend fun getDownSyncPeopleCount(projectId: String, peopleOperationsParams: List<PeopleDownSyncOperation>): List<PeopleCount> =
        if (peopleOperationsParams.isNotEmpty()) {
            makeRequestForPeopleOperations(projectId, peopleOperationsParams)
        } else {
            throw EmptyPeopleOperationsParamsException()
        }

    private suspend fun makeRequestForPeopleOperations(projectId: String, peopleOperationsParams: List<PeopleDownSyncOperation>): List<PeopleCount> =
        makeNetworkRequest({
            val response = it.requestPeopleOperations(
                projectId,
                buildApiPeopleOperations(peopleOperationsParams))

            response.groups.map { responseGroup ->
                val countsForSyncScope = responseGroup.counts
                PeopleCount(
                    countsForSyncScope.create,
                    countsForSyncScope.update,
                    countsForSyncScope.delete)
            }
        }, "countRequest")

    private fun buildApiPeopleOperations(peopleOperationsParams: List<PeopleDownSyncOperation>) =
        ApiPeopleOperations(buildGroups(peopleOperationsParams))

    private fun buildGroups(peopleOperationsParams: List<PeopleDownSyncOperation>) =
        peopleOperationsParams.map {
            val whereLabels = mutableListOf<ApiPeopleOperationWhereLabel>()
            val userId = it.userId
            val moduleId = it.moduleId

            if (userId?.isNotEmpty() == true) {
                whereLabels.add(ApiPeopleOperationWhereLabel(USER.key, userId))
            }

            if (moduleId?.isNotEmpty() == true) {
                whereLabels.add(ApiPeopleOperationWhereLabel(MODULE.key, moduleId))
            }

            whereLabels.add(ApiPeopleOperationWhereLabel(MODE.key, PipeSeparatorWrapperForURLListParam(*it.modes.toTypedArray()).toString()))

            val lastKnownInfo = with(it.lastResult) {
                if (this@with?.lastPatientId?.isNotEmpty() == true && this@with.lastPatientUpdatedAt != null) {
                    ApiLastKnownPatient(this@with.lastPatientId, this@with.lastPatientUpdatedAt)
                } else {
                    null
                }
            }

            ApiPeopleOperationGroup(lastKnownInfo, whereLabels)
        }

    private suspend fun <T> makeNetworkRequest(block: suspend (client: PeopleRemoteInterface) -> T, traceName: String): T =
        retrySimNetworkCalls(getPeopleApiClient(), block, traceName)


    override suspend fun getPeopleApiClient(): PeopleRemoteInterface {
        val token = remoteDbManager.getCurrentToken()
        return simApiClientFactory.build<PeopleRemoteInterface>(token).api
    }
}
