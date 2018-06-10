package com.simprints.id.data.db.remote.network

import com.simprints.id.domain.Project
import com.simprints.id.network.NetworkConstants
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjectRemoteInterface {

    companion object {
        const val baseUrl = NetworkConstants.baseUrl
    }

    @GET("projects/{projectId}")
    fun project(
        @Path("projectId") projectId: String): Single<Response<Project>>
}
