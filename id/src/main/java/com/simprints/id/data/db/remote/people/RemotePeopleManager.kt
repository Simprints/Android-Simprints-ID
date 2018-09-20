package com.simprints.id.data.db.remote.people

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Completable
import io.reactivex.Single

interface RemotePeopleManager {

    fun downloadPerson(patientId: String, projectId: String): Single<fb_Person>
    fun uploadPerson(fbPerson: fb_Person): Completable
    fun uploadPeople(projectId: String, patientsToUpload: ArrayList<fb_Person>): Completable
    fun getNumberOfPatientsForSyncParams(syncParams: SyncTaskParameters): Single<Int>
    fun getPeopleApiClient(): Single<PeopleRemoteInterface>
}
