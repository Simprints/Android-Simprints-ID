package com.simprints.id.data.db.remote

import com.simprints.id.data.db.remote.models.fb_Person

interface RemotePeopleManager {

    suspend fun uploadPeople(projectId: String, patientsToUpload: ArrayList<fb_Person>)

}
