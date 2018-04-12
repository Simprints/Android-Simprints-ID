package com.simprints.id.data.db.remote.network

import com.simprints.id.BuildConfig
import com.simprints.id.data.db.models.Project
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ProjectRemoteInterface {

    companion object {
        private const val apiVersion = "2018-1-0-dev7"
        var baseUrl = "https://$apiVersion-dot-project-management-dot-${BuildConfig.GCP_PROJECT}.appspot.com"
    }

    @GET("/projects/byId")
    fun project(
        @Query("id") projectId: String): Single<Project>
}
