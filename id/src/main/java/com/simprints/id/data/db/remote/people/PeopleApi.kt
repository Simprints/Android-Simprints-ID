package com.simprints.id.data.db.remote.people

import com.simprints.id.data.db.remote.people.models.RemotePeopleToPost
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PeopleApi {

    @POST(PEOPLE_PATH)
    fun uploadPeople(@Header(AUTHORIZATION_HEADER) authorization: String,
                     @Path(PROJECT_ID_PATH_PARAM) projectId: String,
                     @Body people: RemotePeopleToPost): Call<ResponseBody>

    companion object {

        const val AUTHORIZATION_HEADER = "authorization"
        const val PROJECT_ID_PATH_PARAM = "projectId"
        const val PEOPLE_PATH = "/projects/{$PROJECT_ID_PATH_PARAM}/patients"

    }

    object Factory {

        fun build(baseUrl: String, okHttpClient: OkHttpClient): PeopleApi =
            Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build()
                .create(PeopleApi::class.java)

    }


}
