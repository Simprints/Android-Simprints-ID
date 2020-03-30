package com.simprints.id.secure

import com.google.firebase.FirebaseApp

class RemoteProjectInfoProviderImpl : RemoteProjectInfoProvider {

    override fun getProjectName(): String = FirebaseApp.getInstance().options.projectId ?: ""

}
