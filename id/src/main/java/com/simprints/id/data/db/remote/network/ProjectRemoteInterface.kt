package com.simprints.id.data.db.remote.network

import com.simprints.id.BuildConfig
import com.simprints.id.data.db.models.Project
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjectRemoteInterface {

    companion object {
        private const val apiVersion = "2018-1-0-dev8"
        var baseUrl = "https://$apiVersion-dot-project-management-dot-${BuildConfig.GCP_PROJECT}.appspot.com"
    }

    @GET("/projects/id/{id}")
    fun project(
        @Path("id") projectId: String): Single<Project>
}
