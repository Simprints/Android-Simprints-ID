package com.simprints.id.data.db.remote.people

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.people.models.RemotePeopleToPost
import com.simprints.id.data.db.remote.people.models.toRemotePerson
import ru.gildor.coroutines.retrofit.await

class PeopleClient(private val peopleApi: PeopleApi,
                   private val getAuthToken: suspend () -> String) {

    suspend fun uploadPeople(projectId: String, people: List<fb_Person>) {
        val authorization = "Bearer: ${getAuthToken()}"
        val remotePeopleToPost = RemotePeopleToPost(people.map(fb_Person::toRemotePerson))
        peopleApi
            .uploadPeople(authorization, projectId, remotePeopleToPost)
            .await()
    }

}
