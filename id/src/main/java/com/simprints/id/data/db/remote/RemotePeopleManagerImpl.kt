package com.simprints.id.data.db.remote

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.people.PeopleClient

class RemotePeopleManagerImpl(
    private val peopleClient: PeopleClient
) : RemotePeopleManager {

    override suspend fun uploadPeople(projectId: String, people: List<fb_Person>) =
        peopleClient.uploadPeople(projectId, people)

}
