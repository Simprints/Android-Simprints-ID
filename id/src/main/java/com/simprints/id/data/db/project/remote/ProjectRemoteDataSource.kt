package com.simprints.id.data.db.project.remote

import com.google.gson.JsonElement
import com.simprints.id.data.db.project.domain.Project
import io.reactivex.Single


interface ProjectRemoteDataSource {

    fun loadProjectFromRemote(projectId: String): Single<Project>
    fun loadProjectRemoteConfigSettingsJsonString(projectId: String): Single<JsonElement>
    fun getProjectApiClient(): Single<ProjectRemoteInterface>
}
