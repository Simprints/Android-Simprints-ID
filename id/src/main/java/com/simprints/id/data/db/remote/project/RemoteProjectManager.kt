package com.simprints.id.data.db.remote.project

import com.google.gson.JsonElement
import com.simprints.id.data.db.remote.network.ProjectRemoteInterface
import com.simprints.id.domain.Project
import io.reactivex.Single

interface RemoteProjectManager {

    fun loadProjectFromRemote(projectId: String): Single<Project>
    fun loadProjectRemoteConfigSettingsJsonString(projectId: String): Single<JsonElement>
    fun getProjectApiClient(): Single<ProjectRemoteInterface>
}
